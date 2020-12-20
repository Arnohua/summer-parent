package com.dh.limit.service;

import com.dh.limit.annotation.DhLimit;
import com.dh.limit.constants.LimitModelEnum;

import java.lang.reflect.Method;

public interface ILimitService {

    LimitModelEnum model();

    boolean process(DhLimit dhLimit, Method method);
}
