package com.dh.cache.service.impl;

import com.dh.cache.service.ICacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */

public class RedisCacheServiceImpl implements ICacheService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Object get(String region,Object var,Object ...args) {
        return redisTemplate.opsForValue().get(var);
    }

    @Override
    public void set(String region,Object var,Object obj,long expireTime) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(var, obj);
        if(result){
            redisTemplate.expire(var,expireTime, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
