package com.dh.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.PROPERTIES;
import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.YAML;

/**
 * 加载consul default-context节点的配置文件
 * @author dinghua
 * @date 2019/10/14
 * @since v1.0.0
 */
public class ParentConsulPropertySource extends EnumerablePropertySource<ConsulClient> {

	private final Map<String, Object> properties = new LinkedHashMap<>();

	private String context;

	private ConsulConfigProperties configProperties;

	private Long initialIndex;

	public ParentConsulPropertySource(String context, ConsulClient source,
									  ConsulConfigProperties configProperties) {
		super(context, source);
		this.context = context;
		this.configProperties = configProperties;

	}

	public void init(String parentDataKey) {
		if (!this.context.endsWith("/")) {
			this.context = this.context + "/";
		}

		Response<List<GetValue>> response = this.source.getKVValues(this.context,
				this.configProperties.getAclToken(), QueryParams.DEFAULT);

		this.initialIndex = response.getConsulIndex();

		final List<GetValue> values = response.getValue();
		ConsulConfigProperties.Format format = this.configProperties.getFormat();
		switch (format) {
		case KEY_VALUE:
			parsePropertiesInKeyValueFormat(values);
			break;
		case PROPERTIES:
		case YAML:
			parsePropertiesWithNonKeyValueFormat(values, format,parentDataKey);
		}
	}

	public Long getInitialIndex() {
		return this.initialIndex;
	}

	/**
	 * Parses the properties in key value style i.e., values are expected to be either a
	 * sub key or a constant.
	 * @param values values to parse
	 */
	protected void parsePropertiesInKeyValueFormat(List<GetValue> values) {
		if (values == null) {
			return;
		}

		for (GetValue getValue : values) {
			String key = getValue.getKey();
			if (!StringUtils.endsWithIgnoreCase(key, "/")) {
				key = key.replace(this.context, "").replace('/', '.');
				String value = getValue.getDecodedValue();
				this.properties.put(key, value);
			}
		}
	}

	/**
	 * Parses the properties using the format which is not a key value style i.e., either
	 * java properties style or YAML style.
	 * @param values values to parse
	 * @param format format in which the values should be parsed
	 */
	protected void parsePropertiesWithNonKeyValueFormat(List<GetValue> values,
			ConsulConfigProperties.Format format,String parentDataKey) {
		if (values == null) {
			return;
		}

		// 只读取default-context节点的配置
		if(!this.context.contains(this.configProperties.getDefaultContext())){
			return;
		}

		for (GetValue getValue : values) {
			String key = getValue.getKey().replace(this.context, "");
			if (parentDataKey.equals(key)) {
				parseValue(getValue, format);
			}
		}
	}

	protected void parseValue(GetValue getValue, ConsulConfigProperties.Format format) {
		String value = getValue.getDecodedValue();
		if (value == null) {
			return;
		}

		Properties props = generateProperties(value, format);

		for (Map.Entry entry : props.entrySet()) {
			this.properties.put(entry.getKey().toString(), entry.getValue());
		}
	}

	protected Properties generateProperties(String value,
			ConsulConfigProperties.Format format) {
		final Properties props = new Properties();

		if (format == PROPERTIES) {
			try {
				props.load(new ByteArrayInputStream(value.getBytes("ISO-8859-1")));
			}
			catch (IOException e) {
				throw new IllegalArgumentException(
						value + " can't be encoded using ISO-8859-1");
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

	protected Map<String, Object> getProperties() {
		return this.properties;
	}

	protected ConsulConfigProperties getConfigProperties() {
		return this.configProperties;
	}

	protected String getContext() {
		return this.context;
	}

	@Override
	public Object getProperty(String name) {
		return this.properties.get(name);
	}

	@Override
	public String[] getPropertyNames() {
		Set<String> strings = this.properties.keySet();
		return strings.toArray(new String[strings.size()]);
	}

}
