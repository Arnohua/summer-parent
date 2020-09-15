package com.dh.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * @author dinghua
 * @date 2020/9/15
 * @since v1.0.0
 */

@Configuration
public class RedissonConfig {

    @Autowired
    private RedisProperties redisProperties;

    private static final String REDIS_URL_PREFIX = "redis://";

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();

        // 优先集群模式
        if(redisProperties.getCluster() != null){
            List<String> nodesObj = redisProperties.getCluster().getNodes();
            if(null != nodesObj && nodesObj.size() > 0){
                int size = nodesObj.size();
                String[] nodes = new String[size];
                for(int i = 0; i < size; i++){
                    String node = nodesObj.get(i);
                    if(!node.startsWith(REDIS_URL_PREFIX)){
                        node = REDIS_URL_PREFIX + node;
                    }
                    nodes[i] = node;
                }
                ClusterServersConfig clusterServersConfig = config.useClusterServers().addNodeAddress(nodes);
                if(redisProperties.getPassword() != null){
                    clusterServersConfig.setPassword(redisProperties.getPassword());
                }
            }
        }
        // 哨兵模式
        else if(redisProperties.getSentinel() != null){
            List<String> nodesObj = redisProperties.getSentinel().getNodes();
            if(null != nodesObj && nodesObj.size() > 0){
                int size = nodesObj.size();
                String[] nodes = new String[size];
                for(int i = 0; i < size; i++){
                    String node = nodesObj.get(i);
                    if(!node.startsWith(REDIS_URL_PREFIX)){
                        node = REDIS_URL_PREFIX + node;
                    }
                    nodes[i] = node;
                }
                SentinelServersConfig sentinelServersConfig = config.useSentinelServers()
                        .addSentinelAddress(nodes)
                        .setMasterName(redisProperties.getSentinel().getMaster())
                        .setDatabase(redisProperties.getDatabase());
                if(redisProperties.getPassword() != null){
                    sentinelServersConfig.setPassword(redisProperties.getPassword());
                }
            }
        }
        // 单机模式
        else {
            boolean ssl = redisProperties.isSsl();
            String prefix = REDIS_URL_PREFIX;
            if(ssl){
                prefix = "rediss://";
            }
            SingleServerConfig singleServerConfig = config.useSingleServer()
                    .setAddress(prefix + redisProperties.getHost() +":" + redisProperties.getPort())
                    .setDatabase(redisProperties.getDatabase());
            if(redisProperties.getPassword() != null){
                singleServerConfig.setPassword(redisProperties.getPassword());
            }

        }
        return Redisson.create(config);
    }
}
