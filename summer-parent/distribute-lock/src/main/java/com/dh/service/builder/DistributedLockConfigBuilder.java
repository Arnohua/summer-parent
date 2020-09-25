package com.dh.service.builder;

import com.alibaba.fastjson.JSON;
import com.dh.annotation.DistributeLock;
import com.dh.config.LockConfig;
import com.dh.entity.DistributedLockConfig;
import com.dh.service.handler.AbstractDistributedLockHandler;
import com.dh.util.Md5Utils;
import jodd.util.StringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.apache.commons.beanutils.PropertyUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author dinghua
 * @date 2020/9/24
 * @since v1.0.0
 */

public class DistributedLockConfigBuilder {

    private final static String LOCK_METHOD_SEPARATOR = "#";

    ApplicationContext applicationContext;

    LockConfig lockConfig;

    public DistributedLockConfigBuilder(){

    }

    public DistributedLockConfigBuilder(LockConfig lockConfig){
        this.lockConfig = lockConfig;
    }

    public void setApplicationContext(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }

    public DistributedLockConfig buildLockConfig(DistributeLock distributedLock, List<Map<String, Object>> lockKeyParams, String methodFullPath) throws Exception {
        DistributedLockConfig distributedLockConfig = new DistributedLockConfig();
        distributedLockConfig.setLockMode(distributedLock.lockMode());
        distributedLockConfig.setAutomaticRelease(distributedLock.automaticRelease());
        distributedLockConfig.setTimeOut(distributedLock.timeout());
        distributedLockConfig.setTryLockTime(distributedLock.tryLockTime());
        if (!StringUtils.isEmpty(distributedLock.handler())) {
            distributedLockConfig.setHandler((AbstractDistributedLockHandler) applicationContext.getBean(distributedLock.handler()));
        }
        distributedLockConfig.setLockKey(generateLockKey(methodFullPath, distributedLockConfig,lockKeyParams));
        return distributedLockConfig;
    }

    public String generateLockKey(String methodFullPath, DistributedLockConfig distributedLockConfig,List<Map<String, Object>> lockKeyParams) throws Exception {
        AbstractDistributedLockHandler abstractDistributedLockHandler = distributedLockConfig.getHandler();
        StringBuilder sb = new StringBuilder(200);
        sb.append(lockConfig.lockRedisPrefixKey).append(methodFullPath);

        // LockHandler
        if (abstractDistributedLockHandler != null) {
            String key = abstractDistributedLockHandler.keyGenerator(distributedLockConfig);
            if (!StringUtils.isEmpty(key)) {
                sb.append(key);
                return sb.toString();
            }
        }

        // LockKey
        if (lockKeyParams != null && lockKeyParams.size() >= 1) {
            sb.append(LOCK_METHOD_SEPARATOR);
            for (Map<String, Object> lockKeyParam : lockKeyParams) {
                for(Map.Entry<String, Object> entry : lockKeyParam.entrySet()){
                    String key = entry.getKey();
                    Object entryValue = entry.getValue();
                    if(!StringUtils.isEmpty(key)){
                        //如果指定了对象属性的，则以对象属性生成key
                        sb.append(Md5Utils.md5Encode16(JSON.toJSONString(PropertyUtils.getProperty(entryValue,key))));
                    }else {
                        //未指定对象属性的，直接把对象序列化作为key
                        sb.append(Md5Utils.md5Encode16(JSON.toJSONString(entryValue)));
                    }

                }
            }
        }
        return sb.toString();
    }
}
