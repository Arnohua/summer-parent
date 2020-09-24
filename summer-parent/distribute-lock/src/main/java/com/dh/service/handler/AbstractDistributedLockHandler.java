package com.dh.service.handler;


import com.dh.entity.DistributedLockConfig;

/**
 * @author Created by dinghua
 */
public abstract class AbstractDistributedLockHandler {

    /**
     * 生成key的算法
     */
    public String keyGenerator(DistributedLockConfig distributedLockConfig) {
        return null;
    }

    /**
     * 获取锁之前执行的方法
     */
    public void doBeforeLock() {

    }

    /**
     * 获取锁之后执行的方法
     */
    public Object doAfterLock(Object object) {
        return object;
    }

    /**
     * 在获取锁出现异常时调用
     * @param throwable
     * @return
     */
    public Object doAfterException(Throwable throwable) {
        return throwable;
    }

    /**
     * 获取锁失败
     * @return
     */
    public Object doAfterFailGetLock(DistributedLockConfig lockConfig){
    	throw new RuntimeException("Get distribute lock error");
    }
}
