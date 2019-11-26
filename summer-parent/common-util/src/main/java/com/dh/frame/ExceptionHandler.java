package com.dh.frame;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程异常处理
 * @author dinghua
 * @date 2019.11.8
 */

public class ExceptionHandler implements Thread.UncaughtExceptionHandler{

    private static Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("线程池内部错误，线程id：{}，线程名称：{}",t.getId(),t.getName(),e);
        }
    }