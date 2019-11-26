package com.dh.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

/**
 * 加载consul default-context节点的配置文件（公共配置文件）
 * @author dinghua
 * @date 2019/10/14
 * @since v1.0.0
 */
@Order(1)
public class ParentConsulPropertySourceLocator implements PropertySourceLocator {

	private static final Logger log = LoggerFactory.getLogger(ParentConsulPropertySourceLocator.class);

	private final ConsulClient consul;

	private final ConsulConfigProperties properties;

	/**
	 * parent默认data节点
	 */
	@Value("${fpx.consul.parent.config.data-key:V1.0.0}")
	private String parentDataKey;

	private final List<String> contexts = new ArrayList<>();

	private final LinkedHashMap<String, Long> contextIndex = new LinkedHashMap<>();

	public ParentConsulPropertySourceLocator(ConsulClient consul,
											 ConsulConfigProperties properties) {
		this.consul = consul;
		this.properties = properties;
	}

	public LinkedHashMap<String, Long> getContextIndexes() {
		return this.contextIndex;
	}

	@Override
	@Retryable(interceptor = "consulRetryInterceptor")
	public PropertySource<?> locate(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) environment;

			List<String> profiles = Arrays.asList(env.getActiveProfiles());

			String prefix = this.properties.getPrefix();

			List<String> suffixes = new ArrayList<>();
			if (this.properties.getFormat() != FILES) {
				suffixes.add("/");
			}
			else {
				suffixes.add(".yml");
				suffixes.add(".yaml");
				suffixes.add(".properties");
			}

			String defaultContext = getContext(prefix,
					this.properties.getDefaultContext());

			for (String suffix : suffixes) {
				this.contexts.add(defaultContext + suffix);
			}
			for (String suffix : suffixes) {
				addProfiles(this.contexts, defaultContext, profiles, suffix);
			}

			Collections.reverse(this.contexts);

			CompositePropertySource composite = new CompositePropertySource("defaultContextConsul");

			for (String propertySourceContext : this.contexts) {
				try {
					ParentConsulPropertySource propertySource = null;
					if (this.properties.getFormat() != FILES) {
						propertySource = create(propertySourceContext,parentDataKey);
					}
					if (propertySource != null) {
						composite.addPropertySource(propertySource);
					}
				}
				catch (Exception e) {
					if (this.properties.isFailFast()) {
						log.error(
								"Fail fast is set and there was an error reading configuration from consul.");
						ReflectionUtils.rethrowRuntimeException(e);
					}
					else {
						log.warn("Unable to load consul config from "
								+ propertySourceContext, e);
					}
				}
			}

			return composite;
		}
		return null;
	}

	private String getContext(String prefix, String context) {
		if (StringUtils.isEmpty(prefix)) {
			return context;
		}
		else {
			return prefix + "/" + context;
		}
	}

	private void addIndex(String propertySourceContext, Long consulIndex) {
		this.contextIndex.put(propertySourceContext, consulIndex);
	}

	private ParentConsulPropertySource create(String context, String parentDataKey) {
		ParentConsulPropertySource propertySource = new ParentConsulPropertySource(context,
				this.consul, this.properties);
		propertySource.init(parentDataKey);
		addIndex(context, propertySource.getInitialIndex());
		return propertySource;
	}

	private void addProfiles(List<String> contexts, String baseContext,
			List<String> profiles, String suffix) {
		for (String profile : profiles) {
			contexts.add(baseContext + this.properties.getProfileSeparator() + profile
					+ suffix);
		}
	}

}
