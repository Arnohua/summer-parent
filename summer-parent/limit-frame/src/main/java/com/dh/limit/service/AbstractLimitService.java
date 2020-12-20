package com.dh.limit.service;

import com.dh.limit.annotation.DhLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public abstract class AbstractLimitService implements ILimitService {

    protected static final Logger log = LoggerFactory.getLogger(AbstractLimitService.class);

    protected String getLimitKey(DhLimit dhLimit, Method method){
        return String.join(method.getDeclaringClass().getName(),".",method.getName(), dhLimit.key());
    }
}
