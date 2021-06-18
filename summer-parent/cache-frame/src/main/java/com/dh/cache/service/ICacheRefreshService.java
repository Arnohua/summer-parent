package com.dh.cache.service;

/**
 * @author dinghua
 * @date 2021/6/15
 * @since v1.0.0
 */

public interface ICacheRefreshService {

    /**
     * 刷新缓存
     * @param messageBody
     */
    void refresh(MessageBody messageBody);
}
