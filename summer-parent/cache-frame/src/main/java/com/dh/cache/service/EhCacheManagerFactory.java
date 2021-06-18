package com.dh.cache.service;

import com.dh.cache.service.impl.LocalCacheServiceImpl;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * @author dinghua
 * @date 2021/6/18
 * @since v1.0.0
 */

public class EhCacheManagerFactory {

    private static CacheManager cacheManager;

    static {
        URL configPath = EhCacheManagerFactory.class.getResource("/cache.xml");
        cacheManager = CacheManager.create(configPath);
    }

    public static CacheManager create(){
        return cacheManager;
    }
}
