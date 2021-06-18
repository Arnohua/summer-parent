package com.dh.cache.service;

/**
 * @author dinghua
 * @date 2021/6/18
 * @since v1.0.0
 */
public class MessageBody {

    private String region;

    private Object cacheKey;

    public MessageBody() {
    }

    public MessageBody(String region, Object cacheKey) {
        this.region = region;
        this.cacheKey = cacheKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Object getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(Object cacheKey) {
        this.cacheKey = cacheKey;
    }
}
