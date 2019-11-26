package com.dh.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author dinghua
 * @date 2019-11-22
 */
@Configuration
@ConditionalOnConsulEnabled
public class ExtendConsulConfigBootstrapConfiguration {

	@Configuration
	@EnableConfigurationProperties
	@Import(ConsulAutoConfiguration.class)
	@ConditionalOnProperty(name = "spring.cloud.consul.config.enabled", matchIfMissing = true)
	protected static class ConsulPropertySourceConfiguration {

		@Autowired
		private ConsulClient consul;

		@Bean
		@ConditionalOnMissingBean
		public ConsulConfigProperties consulConfigProperties() {
			return new ConsulConfigProperties();
		}

		@Bean
		public FindNewConsulNodeSourceLocator findNewConsulNodeSourceLocator(
				ConsulConfigProperties consulConfigProperties) {
			return new FindNewConsulNodeSourceLocator(this.consul, consulConfigProperties);
		}

		@Bean
		public ParentConsulPropertySourceLocator fpxConsulPropertySourceLocator(
				ConsulConfigProperties consulConfigProperties) {
			return new ParentConsulPropertySourceLocator(this.consul, consulConfigProperties);
		}

	}

}
