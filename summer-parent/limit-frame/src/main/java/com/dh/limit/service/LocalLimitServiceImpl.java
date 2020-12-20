package com.dh.limit.service;

import com.dh.limit.annotation.DhLimit;
import com.dh.limit.constants.LimitModelEnum;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LocalLimitServiceImpl extends AbstractLimitService {

    private Map<String, RateLimiter> limitMap = new ConcurrentHashMap<>();

    @Override
    public LimitModelEnum model() {
        return LimitModelEnum.LOCAL;
    }

    @Override
    public boolean process(DhLimit dhLimit, Method method) {
        String limitKey = getLimitKey(dhLimit,method);
        if(log.isDebugEnabled()){
            log.debug("limitKey:{} limit:{} expire:{}",limitKey, dhLimit.limit(), dhLimit.expire());
        }
        if(null == limitMap.get(limitKey)){
            synchronized (LocalLimitServiceImpl.class){
                if(null == limitMap.get(limitKey)){
                    RateLimiter rateLimiter = RateLimiter.create(dhLimit.limit(), dhLimit.expire(), TimeUnit.SECONDS);
                    limitMap.put(limitKey,rateLimiter);
                }
            }
        }
        return limitMap.get(limitKey).tryAcquire();
    }
}
