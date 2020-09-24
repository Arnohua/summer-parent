package com.dh.service;

import com.dh.entity.DistributedLockConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * @author dinghua
 * @date 2020/9/15
 * @since v1.0.0
 */
public class DistributeReentrantLockImpl extends AbstractLock {

    private RLock rLock;

    public DistributeReentrantLockImpl(RLock rLock){
        this.rLock = rLock;
    }

    @Override
    public void doLock(Long timeOut) throws InterruptedException {
        rLock.lock(timeOut, TimeUnit.SECONDS);
    }

    @Override
    public boolean doTryLock(Long tryLockTime, Long timeOut) throws InterruptedException {
        return rLock.tryLock(tryLockTime,timeOut,TimeUnit.SECONDS);
    }

    @Override
    public void doUnLock() {
        rLock.unlock();
    }
}
