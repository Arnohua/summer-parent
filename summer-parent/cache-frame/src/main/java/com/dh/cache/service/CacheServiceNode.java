package com.dh.cache.service;

/**
 * @author dinghua
 * @date 2021/6/16
 * @since v1.0.0
 */

public class CacheServiceNode {

    private CacheServiceNode pre;

    private ICacheService cacheService;

    private CacheServiceNode next;

    public CacheServiceNode(ICacheService cacheService) {
        this.cacheService = cacheService;
    }

    public CacheServiceNode getPre() {
        return pre;
    }

    public void setPre(CacheServiceNode pre) {
        this.pre = pre;
    }

    public ICacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(ICacheService cacheService) {
        this.cacheService = cacheService;
    }

    public CacheServiceNode getNext() {
        return next;
    }

    public void setNext(CacheServiceNode next) {
        this.next = next;
    }

    public CacheServiceNode addFirst(CacheServiceNode node){
        node.next = this;
        this.pre = node;
        return node;
    }
}
