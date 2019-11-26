package com.dh.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.cfg4j.source.context.propertiesprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.PROPERTIES;
import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.YAML;

/**
 *  检测配置文件中是否定义了新的consul节点，如果有则创建,没有则忽略
 *  优先级在加载consul配置之前
 * @author dinghua
 * @date 2019/10/14
 * @since v1.0.0
 */

@Order(-1)
public class FindNewConsulNodeSourceLocator implements PropertySourceLocator {

    private final ConsulClient consul;

    private final ConsulConfigProperties properties;

    private static final Logger log = LoggerFactory.getLogger(ParentConsulPropertySourceLocator.class);

    public FindNewConsulNodeSourceLocator(ConsulClient consul,
                                          ConsulConfigProperties properties) {
        this.consul = consul;
        this.properties = properties;
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        // 初始化consul节点
        init();
        return null;
    }

    /**
     * 是否用本地配置覆盖consul远程配置，默认不覆盖, 覆盖: true / 不覆盖: false
     */
    @Value("${spring.cloud.consul.config.cover: false}")
    private Boolean cover;

    @Value("${spring.profiles.active: prod}")
    private String env;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * key所在的目录前缀，格式为：config/应用名称/
     */
    @Value("#{'${spring.cloud.consul.config.prefix:css}/'.concat('${spring.application.name}/')}")
    private String keyPrefix;

    /**
     * default-context key所在的目录前缀，格式为：config/default-context/
     */
    @Value("#{'${spring.cloud.consul.config.prefix:css}/'.concat('${spring.cloud.consul.config.default-context}/')}")
    private String defaultContextPrefix;

    /**
     * parent默认v1节点
     */
    @Value("${fpx.consul.parent.config.data-key:V1.0.0}")
    private String parentDataKey;

    /**
     * 加载配置信息到consul中
     *
     * @param key     配置的key
     * @param value   配置的值
     * @param keyList 在consul中已存在的配置信息key集合
     */
    private void visitProps(String key, String value, List<String> keyList) {

        // 覆盖已有配置
        if (cover) {
            this.setKVValue(key, value.toString());
        } else {
            // consul上没有该节点则创建
            if (keyList == null || !keyList.contains(key)) {
                this.setKVValue(key, value.toString());
            }
        }
    }


    /**
     * 启动时加载application.yml配置文件信息到consul配置中心
     * 加载到Consul的文件在ClassPathResource中指定
     */
    private void init() {

        //获取default-context中所有的key-value
        if (!this.defaultContextPrefix.endsWith("/")) {
            this.defaultContextPrefix = this.defaultContextPrefix + "/";
        }
        Map<String, String> kvValues = getKVValues(defaultContextPrefix);
        // 区分站点配置文件
        String dataCenter = kvValues.get("fpx.common.data-center");
        if(StringUtils.isEmpty(dataCenter)){
            // 默认深圳站点
            log.error("未获取到站点信息");
            return;
        }
        Map<String, Object> props = getProperties(dataCenter);
        if(props == null || props.isEmpty()){
            return;
        }
        String line = System.getProperty("line.separator");
        StringBuffer str = new StringBuffer();
        int count = 0;
        for (Map.Entry<String, Object> prop : props.entrySet()){
            if(count != 0){
                str.append(line);
            }
            str.append(prop.getKey()).append("=").append(prop.getValue());
            count++;
        }
        log.info("共获取到配置文件键值对 {} 组",count);
        // 获取consul节点
        List<String> keyList = this.getKVKeysOnly();

        log.info("Found keys : {}",keyList);
        visitProps(this.properties.getDataKey(), str.toString(),keyList);

    }

    /**
     * 读取配置文件中的内容
     *
     * @param fixed
     * @return
     */
    private Map<String, Object> getProperties(String fixed) {
        PropertiesProviderSelector propertiesProviderSelector = new PropertiesProviderSelector(
                new PropertyBasedPropertiesProvider(), new YamlBasedPropertiesProvider(), new JsonBasedPropertiesProvider()
        );
        ClassPathResource resource;
        // 只支持properties文件
        if (fixed != null && !fixed.isEmpty()) {
            resource = new ClassPathResource(applicationName + "_" + env + "_" + fixed + ".properties");
        } else {
            resource = new ClassPathResource("application.properties");
        }
        String fileName = resource.getFilename();
        String path = null;
        Map<String, Object> props = new HashMap<>(16);
        try (InputStream input = resource.getInputStream()) {
            log.info("Found config file: " + resource.getFilename() + " in context " + resource.getURL().getPath());
            path = resource.getURL().getPath();
            PropertiesProvider provider = propertiesProviderSelector.getProvider(fileName);
            props = (Map) provider.getProperties(input);
        } catch (IOException e) {
            log.error("Unable to load properties from file: {}", path,e);
        }
        return props;
    }

    public void setKVValue(String key, String value) {
        try {
            this.consul.setKVValue(keyPrefix + key, value);
        } catch (Exception e) {
            log.error("SetKVValue exception: key: {},value: {}", key, value,e);
        }
    }

    /**
     * 获取应用配置的所有key-value信息
     * @param keyPrefix key所在的目录前缀，格式为：config/应用名称/
     * @return 应用配置的所有key-value信息
     */

    public Map<String, String> getKVValues(String keyPrefix) {

        Map<String, String> result = null;

        Response<List<GetValue>> response = this.consul.getKVValues(keyPrefix,
                this.properties.getAclToken(), QueryParams.DEFAULT);
        final List<GetValue> values = response.getValue();
        ConsulConfigProperties.Format format = this.properties.getFormat();
        switch (format) {
            case KEY_VALUE:
                result = parsePropertiesInKeyValueFormat(values);
                break;
            case PROPERTIES:
            case YAML:
                result = parsePropertiesWithNonKeyValueFormat(values, format);
        }
        if(result == null){
            log.error("***** get consul config is empty ****");
        }
        return result == null ? new LinkedHashMap<>() : result;
    }

    protected Map<String, String> parsePropertiesWithNonKeyValueFormat(List<GetValue> values,
                                                                       ConsulConfigProperties.Format format) {
        if (values == null) {
            return null;
        }

        for (GetValue getValue : values) {
            String key = getValue.getKey().replace(this.defaultContextPrefix, "");
            if (this.parentDataKey.equals(key)) {
                return parseValue(getValue, format);
            }
        }
        return null;
    }

    protected Map<String, String> parseValue(GetValue getValue, ConsulConfigProperties.Format format) {
        String value = getValue.getDecodedValue();
        if (value == null) {
            return null;
        }

        Properties props = generateProperties(value, format);

        Map<String,String> map = new LinkedHashMap<>();
        for (Map.Entry entry : props.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return map;
    }

    protected Properties generateProperties(String value,ConsulConfigProperties.Format format) {
        final Properties props = new Properties();

        if (format == PROPERTIES) {
            try {
                props.load(new ByteArrayInputStream(value.getBytes("ISO-8859-1")));
            }catch (IOException e) {
                throw new IllegalArgumentException(value + " can't be encoded using ISO-8859-1");
            }
            return props;
        }
        else if (format == YAML) {
            final YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResources(new ByteArrayResource(value.getBytes()));

            return yaml.getObject();
        }

        return props;
    }

    protected Map<String, String> parsePropertiesInKeyValueFormat(List<GetValue> values) {
        if (values == null) {
            return null;
        }
        Map<String,String> map = new LinkedHashMap<>();
        for (GetValue getValue : values) {
            String key = getValue.getKey();
            if (!StringUtils.endsWithIgnoreCase(key, "/")) {
                key = key.replace(this.defaultContextPrefix, "").replace('/', '.');
                String value = getValue.getDecodedValue();
                map.put(key, value);
            }
        }
        return map;
    }


    /**
     * 获取应用配置的所有key信息
     *
     * @param keyPrefix key所在的目录前缀，格式为：config/应用名称/
     * @return 应用配置的所有key信息
     */

    public List<String> getKVKeysOnly(String keyPrefix) {
        List<String> list = new ArrayList<>();
        try {
            Response<List<String>> response = this.consul.getKVKeysOnly(keyPrefix);

            if (response.getValue() != null) {
                for (String key : response.getValue()) {
                    int index = key.lastIndexOf("/") + 1;
                    String temp = key.substring(index);
                    list.add(temp);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("GetKVKeysOnly exception: keyPrefix: {}", keyPrefix,e);
        }
        return null;
    }

    public List<String> getKVKeysOnly() {
        return this.getKVKeysOnly(keyPrefix);
    }

}
