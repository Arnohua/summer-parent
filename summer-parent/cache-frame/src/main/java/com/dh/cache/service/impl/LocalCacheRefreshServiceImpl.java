package com.dh.cache.service.impl;

import com.dh.cache.service.EhCacheManagerFactory;
import com.dh.cache.service.ICacheRefreshService;
import com.dh.cache.service.MessageBody;
import net.sf.ehcache.Cache;
import org.springframework.stereotype.Service;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */

public class LocalCacheRefreshServiceImpl implements ICacheRefreshService{

    @Override
    public void refresh(MessageBody messageBody) {
        System.out.println("=========删除本地缓存===============");
        Cache cache = EhCacheManagerFactory.create().getCache(messageBody.getRegion());
        if(cache != null){
            cache.remove(messageBody.getCacheKey());
        }
    }
}
