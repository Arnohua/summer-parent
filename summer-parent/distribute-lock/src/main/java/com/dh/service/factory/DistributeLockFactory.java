package com.dh.service.factory;

import com.dh.constant.LockModeConstant;
import com.dh.service.DistributeReadWriteLockImpl;
import com.dh.service.DistributeRedLockImpl;
import com.dh.service.DistributeReentrantLockImpl;
import com.dh.service.ILock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Set;

/**
 * @author dinghua
 * @date 2019/11/22
 * @since v1.0.0
 */
@Component
public class DistributeLockFactory {

    @Autowired
    private RedissonClient redissonClient;

    private Set<RedissonClient> clients;

    public void setClients(Set<RedissonClient> clients){
        this.clients = clients;
    }

    public Set<RedissonClient> getClients(){
        return clients;
    }


    public ILock getLock(int lockMode, String key) throws IllegalAccessException {
        if (lockMode == LockModeConstant.REENTRANT) {
            return new DistributeReentrantLockImpl(redissonClient.getLock(key));
        }
        if (lockMode == LockModeConstant.READWRITELOCK) {
            return new DistributeReadWriteLockImpl(redissonClient.getReadWriteLock(key));
        }
        if (lockMode == LockModeConstant.REDLOCK) {
            if(clients == null){
                throw new IllegalAccessException("redissonClient list must not be null");
            }
            RLock[] locks = new RLock[clients.size()];
            int idx = 0;
            for(RedissonClient redissonClient : clients){
                locks[idx++] = redissonClient.getLock(key);
            }
            return new DistributeRedLockImpl(new RedissonRedLock(locks));
        }
        return new DistributeReentrantLockImpl(redissonClient.getLock(key));
    }


    @PreDestroy
    public void destroy(){
        if(redissonClient != null){
            redissonClient.shutdown();
        }
    }


}
