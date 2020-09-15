package com.dh.service;

import com.dh.entity.DistributedLockConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RReadWriteLock;

/**
 * @author dinghua
 * @date 2020/9/15
 * @since v1.0.0
 */
public class DistributeReadWriteLockImpl extends AbstractLock {

    private RReadWriteLock rReadWriteLock;

    public DistributeReadWriteLockImpl(RReadWriteLock rReadWriteLock){
        this.rReadWriteLock = rReadWriteLock;
    }

    @Override
    public void doLock(Long timeOut) throws InterruptedException {

    }

    @Override
    public boolean doTryLock(Long tryLockTime, Long timeOut) throws InterruptedException {
        return false;
    }

    @Override
    public void doUnLock() {

    }

    @Override
    public Object doProcess(ProceedingJoinPoint pjp, DistributedLockConfig distributedLockConfig) throws Exception {
        return null;
    }
}
