/*
 * Copyright (c) 2019, 4PX and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.dh.cache.util;
import com.dh.cache.config.CacheConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
;import javax.annotation.PostConstruct;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */

public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    @Autowired
    private CacheConfigProperties cacheConfigProperties;

    private static boolean localCacheEnable;

    @PostConstruct
    public void init(){
        localCacheEnable = cacheConfigProperties.getEnable();
    }

    /**
     * 从applicationContext中根据名称获取Bean, 自动转换为所赋值对象的类型
     * @param name Bean名称
     * @param <T> 类泛型
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    public static boolean localCacheEnable(){
        return localCacheEnable;
    }

    /**
     * 从applicationContext中根据类型获取Bean
     * @param requiredType 类型
     * @param <T> 类泛型
     * @return T
     */
    public static <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    /**
     * 实现ApplicationContextAware接口，注入context到静态变量
     * @param applicationContext 应用上下文
     * @throws BeansException Beans异常
     */
    @Override
    public void setApplicationContext( ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }
}
