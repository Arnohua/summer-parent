package com.dh.service;

import com.dh.entity.DistributedLockConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.RedissonRedLock;

import java.util.concurrent.TimeUnit;

/**
 * @author dinghua
 * @date 2020/9/15
 * @since v1.0.0
 */
public class DistributeRedLockImpl extends AbstractLock {

    private RedissonRedLock redissonRedLock;

    public DistributeRedLockImpl(RedissonRedLock redissonRedLock){
        this.redissonRedLock = redissonRedLock;
    }

    @Override
    public void doLock(Long timeOut) throws InterruptedException {
        redissonRedLock.lock(timeOut, TimeUnit.SECONDS);
    }

    @Override
    public boolean doTryLock(Long tryLockTime, Long timeOut) throws InterruptedException {
        return redissonRedLock.tryLock(tryLockTime,timeOut,TimeUnit.SECONDS);
    }

    @Override
    public void doUnLock() {
        redissonRedLock.unlock();
    }
}
