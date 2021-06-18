package com.dh.cache.service.impl;

import com.dh.cache.service.ICacheService;

/**
 * @author dinghua
 * @date 2021/6/18
 * @since v1.0.0
 */

public class MongoDBCacheServiceImpl implements ICacheService {

    @Override
    public Object get(String region, Object var, Object... args) {
        return null;
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
