package com.dh.limit.service;

import com.dh.limit.annotation.DhLimit;
import com.dh.limit.constants.LimitModelEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Collections;

@Service
public class DistributeLimitServiceImpl extends AbstractLimitService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisScript<Long> redisScript;

    @Override
    public LimitModelEnum model() {
        return LimitModelEnum.DISTRIBUTE;
    }

    @Override
    public boolean process(DhLimit dhLimit, Method method) {
        String limitKey = getLimitKey(dhLimit, method);
        Long execute = (Long) redisTemplate.execute(redisScript, Collections.singletonList(limitKey), dhLimit.limit(), dhLimit.expire(),1);
        if(null != execute && execute.longValue() == 1){
            return true;
        }
        return false;
    }
}
