package com.dh.common.util;

import com.dh.frame.ExceptionHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import java.util.concurrent.*;

/**
 * payable线程池工具
 * @author dinghua
 * @date 2019/11/8
 * @since v1.0.0
 */
public class WrapperExecutors {

    private static final int DEFAULT_QUEUE_SIZE = 100;

    private WrapperExecutors(){}

    /**
     * 创建固定线程的线程池,队列容量默认100,如果任务已满，由调用线程处理该任务（阻塞）
     * @param nThread 固定的线程数
     * @param nameFormat 线程池名称
     * @return
     */
    public static ExecutorService newFixedThreadPool(int nThread,String nameFormat){
        Validate.isTrue(nThread > 0,"线程数量不能小于1");
        return new ThreadPoolExecutor(nThread, nThread, 60L,TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(DEFAULT_QUEUE_SIZE),getThreadFactory(nameFormat),
                    new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 创建单线程的线程池,队列容量默认100,如果任务已满，由调用线程处理该任务（阻塞）
     * @param nameFormat 线程池名称
     * @return
     */
    public static ExecutorService newSingleThreadExecutor(String nameFormat){
        return new ThreadPoolExecutor(0, 1, 120L,TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(DEFAULT_QUEUE_SIZE),getThreadFactory(nameFormat),
                    new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 创建线程池
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param threadFactory 线程工厂
     * @param handler 拒绝策略
     * @return
     */
    public static ExecutorService newThreadPool(int corePoolSize,
                                                int maximumPoolSize,
                                                long keepAliveTime,
                                                TimeUnit unit,
                                                BlockingQueue<Runnable> workQueue,
                                                ThreadFactory threadFactory,
                                                RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,threadFactory,handler);

    }

    /**
     * 创建线程池,使用默认的拒绝策略
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param threadFactory
     * @return
     */
    public static ExecutorService newThreadPool(int corePoolSize,
                                                int maximumPoolSize,
                                                long keepAliveTime,
                                                TimeUnit unit,
                                                BlockingQueue<Runnable> workQueue,
                                                ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,threadFactory);
    }

    /**
     * 获取线程工厂
     * @param nameFormat
     * @return
     */
    public static ThreadFactory getThreadFactory(String nameFormat){
        Validate.isTrue(StringUtils.isNotBlank(nameFormat),"线程池名称不能为空");
        return  new ThreadFactoryBuilder()
                    .setNameFormat(nameFormat)
                    .setUncaughtExceptionHandler(new ExceptionHandler())
                    .build();
    }
}

