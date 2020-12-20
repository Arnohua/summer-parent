package com.dh.limit.aspect;
import com.dh.limit.annotation.DhLimit;
import com.dh.limit.service.ILimitService;
import com.dh.limit.service.LimitServiceFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

@Order(1)
@Component
public class LimitAspect extends AspectJExpressionPointcutAdvisor{

    private static final Logger log = LoggerFactory.getLogger(LimitAspect.class);

    @Autowired
    private LimitServiceFactory limitServiceFactory;

    @PostConstruct
    public void init() {
        super.setExpression("@annotation(com.dh.limit.annotation.DhLimit)");
        super.setAdvice((MethodInterceptor) invocation -> before(invocation));
    }

    public Object before(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        DhLimit dhLimit = method.getAnnotation(DhLimit.class);

        if(dhLimit == null){
            return invocation.proceed();
        }
        if(log.isDebugEnabled()){
            log.debug("method {} current limiting effect");
        }
        ILimitService limitService = limitServiceFactory.getBean(dhLimit.LIMIT_MODEL());
        if(limitService.process(dhLimit,method)){
            return invocation.proceed();
        }
        return dhLimit.handler().newInstance().afterLimit();
    }
}
