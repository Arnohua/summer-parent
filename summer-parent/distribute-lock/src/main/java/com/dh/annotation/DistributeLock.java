package com.dh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeLock {

    int lockMode() default 0;

    long timeout() default -1L;

    long tryLockTime() default 0L;

    boolean automaticRelease() default true;

    String handler() default "";
}
