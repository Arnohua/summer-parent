package com.dh.limit.annotation;

import com.dh.limit.constants.LimitModelEnum;
import com.dh.limit.strategy.DefaultLimitHandler;
import com.dh.limit.strategy.LimitHandler;

public @interface DhLimit {

    /**
     * 限流模式，默认是分布式限流
     * @return
     */
    LimitModelEnum LIMIT_MODEL() default LimitModelEnum.DISTRIBUTE;

    /**
     * 限流参数(方法名 + key) 默认是接口限流，可以指定接口中的参数限流
     * @return
     */
    String key() default "";

    /**
     * 单位时间限制通过请求数
     * @return
     */
    long limit() default 10;

    /**
     * 过期时间，单位秒
     * @return
     */
    long expire() default 1;

    /**
     * 限流处理器(触发限流后的处理操作)
     * @return
     */
    Class<? extends LimitHandler> handler() default DefaultLimitHandler.class;
}
