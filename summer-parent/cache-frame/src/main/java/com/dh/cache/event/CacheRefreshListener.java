package com.dh.cache.event;

import com.dh.cache.service.ICacheRefreshNoticeService;
import com.dh.cache.service.ICacheRefreshService;
import com.dh.cache.service.MessageBody;
import com.dh.cache.util.LocalAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dinghua
 * @date 2021/6/11
 * @since v1.0.0
 */

public class CacheRefreshListener implements MessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private List<ICacheRefreshService> cacheRefreshServiceList;

    @Autowired
    private ICacheRefreshNoticeService cacheRefreshNoticeService;

    /** 缓存刷新重试次数*/
    private static final int RETRY_TIMES = 3;

    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshListener.class);

    /** 刷新失败的缓存列表*/
    private static List<ICacheRefreshService> refreshFailList = new ArrayList<>();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
        MessageBody messageBody = (MessageBody) valueSerializer.deserialize(message.getBody());
        for(ICacheRefreshService cacheRefreshService : cacheRefreshServiceList){
            int count = 0;
            /** 默认失败重试三次 */
            while (!onFresh(cacheRefreshService,messageBody) && ++count <= RETRY_TIMES);
        }
        if(!CollectionUtils.isEmpty(refreshFailList)){
            cacheRefreshNoticeService.notify(LocalAddressUtil.getLocalHostLANAddress().getHostAddress(),refreshFailList,messageBody);
            refreshFailList.clear();
        }
    }

    private boolean onFresh(ICacheRefreshService cacheRefreshService,MessageBody messageBody){
        try {
            cacheRefreshService.refresh(messageBody);
        } catch (Exception e){
            logger.error("cache refresh error :",e);
            refreshFailList.add(cacheRefreshService);
            return false;
        }
        refreshFailList.remove(cacheRefreshService);
        return true;
    }
}
