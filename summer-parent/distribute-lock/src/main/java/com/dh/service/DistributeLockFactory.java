package com.dh.service;

import com.dh.config.LockConfig;
import com.dh.util.RedissonConnectionUtil;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author dinghua
 * @date 2019/11/22
 * @since v1.0.0
 */
@Component
public class DistributeLockFactory {

    private RedissonClient redissonClient;

    @Autowired
    private LockConfig lockConfig;

    @PostConstruct
    public void init(){
        redissonClient = RedissonConnectionUtil.getRedissonClient(lockConfig.redisAddress,lockConfig.redisPassword);
    }

    @PreDestroy
    public void destroy(){
        if(redissonClient != null){
            redissonClient.shutdown();
        }
    }
}
