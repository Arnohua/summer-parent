package com.dh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * @author dinghua
 * @date 2019-11-22
 */
@Configuration
public class LockConfig {
    
    @Value("${lock.redis.prefixKey:}")
    public String lockRedisPrefixKey;
    
    @Value("${spring.redis.cluster.nodes:}")
    public String redisAddress;
    
    @Value("${spring.redis.password:}")
    public String redisPassword;
}