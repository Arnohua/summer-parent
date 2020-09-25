package com.dh.service.factory;

import com.dh.constant.LockModeConstant;
import com.dh.service.*;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Set;

/**
 * @author dinghua
 * @date 2019/11/22
 * @since v1.0.0
 */
public class DistributeLockFactory {

    private RedissonClient redissonClient;

    private Set<RedissonClient> clients;

    public DistributeLockFactory(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }

    public void setClients(Set<RedissonClient> clients){
        this.clients = clients;
    }

    public Set<RedissonClient> getClients(){
        return clients;
    }


    public ILock getLock(int lockMode, String key) throws IllegalAccessException {
        AbstractLock lock;
        switch (lockMode) {
            case LockModeConstant.REENTRANT_LOCK:
                lock = new DistributeReentrantLockImpl(redissonClient.getLock(key));
                break;
            case LockModeConstant.READ_LOCK:
                lock = new DistributeReadLockImpl(redissonClient.getReadWriteLock(key));
                break;
            case LockModeConstant.WRITE_LOCK:
                lock = new DistributeWriteLockImpl(redissonClient.getReadWriteLock(key));
                break;
            case LockModeConstant.RED_LOCK:
                if (clients == null) {
                    throw new IllegalAccessException("redissonClient list must not be null");
                }
                RLock[] locks = new RLock[clients.size()];
                int idx = 0;
                for (RedissonClient redissonClient : clients) {
                    locks[idx++] = redissonClient.getLock(key);
                }
                lock = new DistributeRedLockImpl(new RedissonRedLock(locks));
                break;
            default:
                lock = new DistributeReentrantLockImpl(redissonClient.getLock(key));
        }
        return lock;
    }

    public void destroy(){
        if(redissonClient != null){
            redissonClient.shutdown();
        }
    }


}
