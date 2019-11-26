package com.dh.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author dinghua
 * @date 2019/11/22
 * @since v1.0.0
 */

@Component
@Aspect
public class DistributeLockAspect {

    @Pointcut("@annotation(com.dh.annotation.DistributeLock)")
    public void pointCut(){}

    @Around("pointCut()")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {

        return joinPoint.proceed();
    }
}
