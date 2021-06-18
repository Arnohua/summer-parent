package com.dh.cache.config;

import com.dh.cache.service.ICacheRefreshNoticeService;
import com.dh.cache.service.ICacheRefreshService;
import com.dh.cache.service.ICacheService;
import com.dh.cache.service.impl.DefaultCacheRefreshNoticeServiceImpl;
import com.dh.cache.service.impl.LocalCacheRefreshServiceImpl;
import com.dh.cache.service.impl.LocalCacheServiceImpl;
import com.dh.cache.service.impl.RedisCacheServiceImpl;
import com.dh.cache.util.SpringContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dinghua
 * @date 2021/6/16
 * @since v1.0.0
 */


@Configuration
public class CacheAutoConfiguration {

    @Bean("cacheRefreshService")
    public ICacheRefreshService cacheRefreshService(){
        return new LocalCacheRefreshServiceImpl();
    }

    @Bean("localCacheService")
    @ConditionalOnProperty(value = "common.cache.local.enable",havingValue = "true",matchIfMissing = true)
    public ICacheService localCacheService(){
        return new LocalCacheServiceImpl();
    }

    @Bean("redisCacheService")
    public ICacheService redisCacheService(){
        return new RedisCacheServiceImpl();
    }

    @Bean("springContextHolder")
    public SpringContextHolder contextHolder(){
        return new SpringContextHolder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ICacheRefreshNoticeService cacheRefreshNoticeService(){
        return new DefaultCacheRefreshNoticeServiceImpl();
    }
}
