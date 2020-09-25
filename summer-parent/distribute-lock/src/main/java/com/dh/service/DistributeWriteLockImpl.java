package com.dh.service;

import org.redisson.api.RReadWriteLock;

import java.util.concurrent.TimeUnit;

/**
 * @author dinghua
 * @date 2020/9/25
 * @since v1.0.0
 */
public class DistributeWriteLockImpl extends AbstractLock {

    private RReadWriteLock rReadWriteLock;

    public DistributeWriteLockImpl(RReadWriteLock rReadWriteLock){
        this.rReadWriteLock = rReadWriteLock;
    }

    @Override
    public void doLock(Long timeOut) throws InterruptedException {
        rReadWriteLock.writeLock().lock(timeOut,TimeUnit.SECONDS);
    }

    @Override
    public boolean doTryLock(Long tryLockTime, Long timeOut) throws InterruptedException {
        return rReadWriteLock.writeLock().tryLock(tryLockTime,timeOut,TimeUnit.SECONDS);
    }

    @Override
    public void doUnLock() {
        rReadWriteLock.writeLock().unlock();
    }
}
