package com.dh.limit.config;

import com.dh.limit.aspect.LimitAspect;
import com.dh.limit.service.DistributeLimitServiceImpl;
import com.dh.limit.service.LimitServiceFactory;
import com.dh.limit.service.LocalLimitServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;


@Configuration
@ConditionalOnBean(RedisTemplate.class)
@Import({DistributeLimitServiceImpl.class,LocalLimitServiceImpl.class,LimitServiceFactory.class, LimitAspect.class})
public class EnableLimitConfiguration {

    @Bean("redisScript")
    public DefaultRedisScript getRedisScript() {
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setLocation(new ClassPathResource("limitScript.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
