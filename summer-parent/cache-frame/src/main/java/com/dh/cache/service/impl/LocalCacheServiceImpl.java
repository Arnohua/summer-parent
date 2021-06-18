package com.dh.cache.service.impl;

import com.dh.cache.service.EhCacheManagerFactory;
import com.dh.cache.service.ICacheService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */

public class LocalCacheServiceImpl implements ICacheService{

    private CacheManager cacheManager;

    private static final Logger logger = LoggerFactory.getLogger(LocalCacheServiceImpl.class);

    public LocalCacheServiceImpl(){
        cacheManager = EhCacheManagerFactory.create();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Object get(String region,Object var,Object ...args) {
        System.out.println("=========local=========");
        Cache cache = cacheManager.getCache("test");
        if(null != cache){
            Element element = cache.get(var);
            Object objectValue = element.getObjectValue();
            if(logger.isInfoEnabled()){
                logger.debug("get value from local cache, cache key is :", var);
            }
            return objectValue;
        }
        return null;
    }

    @Override
    public void set(String region,Object var,Object obj,long expireTime) {
        Cache cache = cacheManager.getCache("test");
        if(null == cache){
            synchronized (LocalCacheServiceImpl.class){
                if(null == cache){
                    cache = new Cache("test",1000,false,false,expireTime,expireTime);
                    cacheManager.addCache(cache);
                }
            }
        }
        Element element = new Element(var,obj);
        cache.put(element);
        System.out.println("=========set local==========");
    }
}
