package com.dh.aspect;

import com.dh.annotation.DistributeLock;
import com.dh.annotation.LockKey;
import com.dh.entity.DistributedLockConfig;
import com.dh.service.ILock;
import com.dh.service.builder.DistributedLockConfigBuilder;
import com.dh.service.factory.DistributeLockFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dinghua
 * @date 2019/11/22
 * @since v1.0.0
 */

@Component
@Aspect
@Order(1)
public class DistributeLockAspect {

    @Autowired
    private DistributeLockFactory distributeLockFactory;

    @Autowired
    private DistributedLockConfigBuilder configBuilder;

    @Pointcut("@annotation(com.dh.annotation.DistributeLock)")
    public void pointCut(){}

    @Around("pointCut()")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributeLock distributeLock = method.getAnnotation(DistributeLock.class);
        Object[] args = joinPoint.getArgs();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<Map<String, Object>> lockKeyParams = new ArrayList<>(2);
        if(parameterAnnotations != null){
            int length = parameterAnnotations.length;
            for(int i = 0;i < length; i++){
                Annotation[] annotations = parameterAnnotations[i];
                for(Annotation an : annotations){
                    if(an instanceof LockKey){
                        LockKey lockKey = (LockKey) an;
                        Map<String,Object> lockParam = new HashMap<>();
                        lockParam.put(lockKey.value(),args[i]);
                        lockKeyParams.add(lockParam);
                    }
                }
            }
        }
        String methodFullPath = String.format("%s.%s",joinPoint.getTarget().getClass().getName(),method.getName());
        DistributedLockConfig distributedLockConfig = configBuilder.buildLockConfig(distributeLock, lockKeyParams, methodFullPath);
        ILock lock = distributeLockFactory.getLock(distributedLockConfig.getLockMode(), distributedLockConfig.getLockKey());
        return lock.doProcess(joinPoint,distributedLockConfig);
    }
}
