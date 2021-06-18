package com.dh.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author dinghua
 * @date 2021/6/16
 * @since v1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "common.cache.local")
public class CacheConfigProperties {

    /** 是否开启本地缓存  默认开启*/
    private boolean enable = true;

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
