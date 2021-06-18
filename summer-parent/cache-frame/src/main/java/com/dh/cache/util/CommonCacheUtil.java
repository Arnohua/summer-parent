package com.dh.cache.util;

import com.dh.cache.constants.CacheConstant;
import com.dh.cache.service.HandlerExecutionChain;
import com.dh.cache.service.ICacheService;
import org.springframework.util.Assert;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */

public class CommonCacheUtil {

    public static <T> T get(String region,Object key,HandlerExecutionChain handlerExecutionChain,Object ...args){
        Assert.notNull(handlerExecutionChain,"handlerExecutionChain must not be null");
        Assert.notNull(region,"region must not be null");
        Assert.notNull(key,"key must not be null");
        Object apply = handlerExecutionChain.apply(region,key,args);
        return apply == null ? null : (T) apply;
    }

    public static <T> T get(String region,Object key, ICacheService lastCacheService,Object ...args){
        Assert.notNull(region,"region must not be null");
        Assert.notNull(key,"key must not be null");
        HandlerExecutionChain handlerExecutionChain = new HandlerExecutionChain();
        handlerExecutionChain.addCacheService(lastCacheService);
        Object apply = handlerExecutionChain.apply(region,key, args);
        return apply == null ? null : (T) apply;
    }

    public static <T> T get(Object key, ICacheService lastCacheService,Object ...args){
        return get(CacheConstant.DEFAULT_REGION,key,lastCacheService,args);
    }

    public static <T> T get(Object key,HandlerExecutionChain handlerExecutionChain,Object ...args){
        return get(CacheConstant.DEFAULT_REGION,key,handlerExecutionChain,args);
    }
}
