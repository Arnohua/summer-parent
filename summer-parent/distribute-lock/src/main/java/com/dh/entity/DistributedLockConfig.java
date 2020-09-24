package com.dh.entity;

import com.dh.service.handler.AbstractDistributedLockHandler;

import java.util.List;
import java.util.Map;

/**
 * @author dinghua
 */
public class DistributedLockConfig {

    private Integer lockMode;

    private Long timeOut;

    private boolean automaticRelease;

    private Long tryLockTime;

    private String lockKey;

    public Integer getLockMode() {
        return lockMode;
    }

    public void setLockMode(Integer lockMode) {
        this.lockMode = lockMode;
    }

    public Long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Long timeOut) {
        this.timeOut = timeOut;
    }

    public boolean isAutomaticRelease() {
        return automaticRelease;
    }

    public void setAutomaticRelease(boolean automaticRelease) {
        this.automaticRelease = automaticRelease;
    }

    public Long getTryLockTime() {
        return tryLockTime;
    }

    public void setTryLockTime(Long tryLockTime) {
        this.tryLockTime = tryLockTime;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public AbstractDistributedLockHandler getHandler() {
        return handler;
    }

    public void setHandler(AbstractDistributedLockHandler handler) {
        this.handler = handler;
    }

    private AbstractDistributedLockHandler handler;

}
