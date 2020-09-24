package com.dh.service;

import com.dh.entity.DistributedLockConfig;
import com.dh.service.handler.AbstractDistributedLockHandler;
import com.dh.service.handler.DefaultDistributeLockHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dinghua
 * @date 2020/9/15
 * @since v1.0.0
 */
public abstract class AbstractLock implements ILock{

    protected final Logger logger = LoggerFactory.getLogger(AbstractLock.class);

    @Override
    public Object doProcess(ProceedingJoinPoint pjp, DistributedLockConfig distributedLockConfig) throws Exception {
        AbstractDistributedLockHandler handler = distributedLockConfig.getHandler();
        Object result;
        if (handler == null) {
            handler = new DefaultDistributeLockHandler();
        }
        boolean isGetLockSuccess = true;
        try {
            isGetLockSuccess = doTryLock(distributedLockConfig.getTryLockTime(), distributedLockConfig.getTimeOut());
            if (!isGetLockSuccess) {
                logger.error("尝试获取锁失败，超过最大等待时间.配置为{}，切点为{}", distributedLockConfig, pjp);
                return handler.doAfterFailGetLock(distributedLockConfig);
            }
            handler.doBeforeLock();
            result = pjp.proceed();
            result = handler.doAfterLock(result);
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage());
            result = handler.doAfterException(throwable);
            if(throwable instanceof Exception){
                throw (Exception)throwable;
            }else{
                throw new Exception(throwable);
            }
        } finally {
            if (distributedLockConfig.isAutomaticRelease() && isGetLockSuccess) {
                try {
                    doUnLock();
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        }

        return result;
    }
}
