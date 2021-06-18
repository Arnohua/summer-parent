package com.dh.cache.service.impl;

import com.dh.cache.service.ICacheRefreshNoticeService;
import com.dh.cache.service.ICacheRefreshService;
import com.dh.cache.service.MessageBody;

import java.util.List;

/**
 * @author dinghua
 * @date 2021/6/18
 * @since v1.0.0
 */

public class DefaultCacheRefreshNoticeServiceImpl implements ICacheRefreshNoticeService{

    @Override
    public void notify(String address, List<ICacheRefreshService> refreshFailList, MessageBody messageBody) {
        System.out.println("=======" + address);
    }
}
