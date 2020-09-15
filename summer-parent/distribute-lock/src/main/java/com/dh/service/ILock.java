package com.dh.service;

import com.dh.entity.DistributedLockConfig;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author dinghua
 * @date 2020/9/15
 * @since v1.0.0
 */
public interface ILock {

    /**
     * 加锁
     *
     * @param timeOut 超时时间
     * @throws InterruptedException 有可能抛出该异常
     */
    void doLock(Long timeOut) throws InterruptedException;

    /**
     * 尝试加锁
     *
     * @param tryLockTime 尝试等待获取锁的时间
     * @param timeOut     超时时间
     * @return 是否成功获取到锁
     * @throws InterruptedException 有可能抛出该异常
     */
    boolean doTryLock(Long tryLockTime, Long timeOut) throws InterruptedException;

    /**
     * 释放锁
     */
    void doUnLock();

    /**
     * 执行分布式锁整个工作
     * @param pjp
     * @param distributedLockConfig
     * @return Object
     */
    Object doProcess(ProceedingJoinPoint pjp, DistributedLockConfig distributedLockConfig) throws Exception;


}
