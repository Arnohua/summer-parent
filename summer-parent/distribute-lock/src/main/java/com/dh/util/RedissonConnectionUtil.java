package com.dh.util;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import javax.xml.bind.ValidationException;

/**
 * redissonClient工具类
 * @author dinghua
 * @date 2019/11/22
 * @since v1.0.0
 */
public class RedissonConnectionUtil {

    private static volatile RedissonClient redissonClient;

    private static Logger logger = LoggerFactory.getLogger(RedissonConnectionUtil.class);

    private RedissonConnectionUtil(){}

    public static RedissonClient getRedissonClient(String redisAddresses) {

        if(redissonClient == null){
            synchronized (RedissonConnectionUtil.class){
                if(redissonClient == null){
                    try {
                        initRedissonClient(redisAddresses,null);
                    } catch (ValidationException e) {
                       logger.error(e.getMessage());
                    }
                }
            }
        }
        return redissonClient;
    }

    public static RedissonClient getRedissonClient(String redisAddresses,String password) {
        if(redissonClient == null){
            synchronized (RedissonConnectionUtil.class){
                if(redissonClient == null){
                    try {
                        initRedissonClient(redisAddresses,password);
                    } catch (ValidationException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
        return redissonClient;
    }

    private static void initRedissonClient(String redisAddresses, String password) throws ValidationException {
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config.useClusterServers().setScanInterval(2000);
        if(StringUtils.isEmpty(redisAddresses)){
            throw new ValidationException("redisAddress不能为空!");
        }
        String[] addresses = redisAddresses.trim().split(",");
        for(String address : addresses){
            if(!StringUtils.isEmpty(address)){
                clusterServersConfig.addNodeAddress("redis://" + address);
            }
        }

        if(!StringUtils.isEmpty(password)){
            clusterServersConfig.setPassword(password);
        }
        redissonClient = Redisson.create(config);
    }
}
