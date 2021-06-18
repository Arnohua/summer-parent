package com.dh.cache.service;

import com.dh.cache.util.SpringContextHolder;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */
public class HandlerExecutionChain {

    private List<ICacheService> cacheServiceList;

    private CacheServiceNode cacheServiceNode;

    private long expireTime;

    private static final Executor EXECUTOR = new ThreadPoolExecutor(0,1, 120L,TimeUnit.SECONDS,new SynchronousQueue<>(),new ThreadPoolExecutor.DiscardPolicy());

    public HandlerExecutionChain(){
        this.cacheServiceList = new ArrayList<>();
        if(SpringContextHolder.localCacheEnable()){
            cacheServiceList.add(SpringContextHolder.getBean("localCacheService"));
        }
        cacheServiceList.add(SpringContextHolder.getBean("redisCacheService"));
    }

    public HandlerExecutionChain(List<ICacheService> cacheServices){
        this.cacheServiceList = cacheServices;
    }

    public long getExpireTime() {
        return expireTime <= 0 ? -1 : expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public void setCacheServiceList(List<ICacheService> cacheServiceList) {
        this.cacheServiceList = cacheServiceList;
    }

    public synchronized void addCacheService(ICacheService cacheService){
        if(null == cacheServiceList){
            this.cacheServiceList = new ArrayList<>();
        }
        if(null != cacheService){
            cacheServiceList.add(cacheService);
        }
    }

    public List<ICacheService> getCacheServiceList() {
        return cacheServiceList;
    }

    public CacheServiceNode getCacheServiceNode(){
        if(CollectionUtils.isEmpty(cacheServiceList)){
            return null;
        }
        Collections.sort(this.cacheServiceList, Comparator.comparingInt(Ordered::getOrder));
        int size = cacheServiceList.size();

        for(int i = size - 1; i >= 0; i--){
            if(i == size - 1){
                cacheServiceNode = new CacheServiceNode(cacheServiceList.get(i));
                continue;
            }
            CacheServiceNode tempNode = new CacheServiceNode(cacheServiceList.get(i));
            cacheServiceNode = cacheServiceNode.addFirst(tempNode);
        }
        return cacheServiceNode;
    }

    public Object apply(String region,Object var,Object ...args){
        CacheServiceNode cacheServiceNode = getCacheServiceNode();
        while (cacheServiceNode != null){
            ICacheService cacheService = cacheServiceNode.getCacheService();
            Object o = cacheService.get(region,var,args);
            if(o != null){
                CacheServiceNode finalCacheServiceNode = cacheServiceNode;
                EXECUTOR.execute(() -> {
                    synchronized (var){
                        CacheServiceNode preNode = finalCacheServiceNode.getPre();
                        while (preNode != null){
                            preNode.getCacheService().set(region,var,o,getExpireTime());
                            preNode = preNode.getPre();
                        }
                    }
                });

                return o;
            }
            cacheServiceNode = cacheServiceNode.getNext();
        }
        return null;
    }
}
