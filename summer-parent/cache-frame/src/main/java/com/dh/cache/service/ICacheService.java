package com.dh.cache.service;

import org.springframework.core.Ordered;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */
public interface ICacheService extends Ordered{

    /**
     *  获取数据
     *  @param region
     * @param var 缓存key
     * @param args 其他入参
     * @return
     */
    Object get(String region,Object var,Object ...args);

    /**
     * 设置缓存数据
     * @param region
     * @param var 缓存key
     * @param obj 缓存value
     * @param expireTime 缓存失效时间
     */
    default void set(String region,Object var,Object obj,long expireTime){}
}
