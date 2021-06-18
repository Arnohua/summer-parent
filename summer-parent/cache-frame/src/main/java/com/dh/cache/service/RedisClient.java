package com.dh.cache.service;

import com.dh.cache.constants.CacheConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */

@Service
public class RedisClient {

    @Autowired
    private RedisTemplate redisTemplate;

    public Boolean delete(String region,Object obj){
        Boolean delete = redisTemplate.delete(obj);
        if(delete){
            if(StringUtils.isEmpty(region)){
                region = CacheConstant.DEFAULT_REGION;
            }
            MessageBody messageBody = new MessageBody(region,obj);
            redisTemplate.convertAndSend(CacheConstant.CACHE_REFRESH_TOPIC, messageBody);
        }
        return delete;
    }
}
