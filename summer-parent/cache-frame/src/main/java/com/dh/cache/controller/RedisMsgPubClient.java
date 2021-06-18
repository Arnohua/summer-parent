package com.dh.cache.controller;

import com.dh.cache.constants.CacheConstant;
import com.dh.cache.service.ICacheService;
import com.dh.cache.service.MessageBody;
import com.dh.cache.service.RedisClient;
import com.dh.cache.util.CommonCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/pub")
public class RedisMsgPubClient {
    private Logger logger = LoggerFactory.getLogger(RedisMsgPubClient.class);
    
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    private RedisClient redisClient;
    
    @RequestMapping("/test")
    @ResponseBody
    public String pubMsg() {
        logger.info("发布消息：");
        MessageBody messageBody = new MessageBody(CacheConstant.DEFAULT_REGION,"test");
        redisTemplate.convertAndSend(CacheConstant.CACHE_REFRESH_TOPIC, messageBody);
        return "成功";
    }

    @RequestMapping("/test2")
    @ResponseBody
    public String test2() {
        redisClient.delete(null,"test");
        return "test";
    }

    @RequestMapping("/test1")
    @ResponseBody
    public String test() {
        Object o = CommonCacheUtil.get("test","test", new ICacheService() {

            @Override
            public Object get(String region,Object var, Object... args) {
                System.out.println("mysql");
                return "MYSQL";
            }

            @Override
            public int getOrder() {
                return 10;
            }
        });
        return o.toString();
    }
}