package com.dh.cache.service;

import java.util.List;

/**
 * @author dinghua
 * @date 2021/6/18
 * @since v1.0.0
 */
public interface ICacheRefreshNoticeService {

    /**
     * 通知
     * @param address
     * @param refreshFailList
     * @param messageBody
     */
    void notify(String address, List<ICacheRefreshService> refreshFailList,MessageBody messageBody);
}
