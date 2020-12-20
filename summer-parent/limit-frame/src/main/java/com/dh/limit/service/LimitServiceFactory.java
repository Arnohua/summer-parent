package com.dh.limit.service;

import com.dh.limit.constants.LimitModelEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LimitServiceFactory implements InitializingBean {
    @Autowired
    private List<ILimitService> limitServiceList;

    private static Map<LimitModelEnum,ILimitService> limitMap = new ConcurrentHashMap<>(8);

    @Override
    public void afterPropertiesSet() throws Exception {
        if(null != limitServiceList){
            for(ILimitService limitService : limitServiceList){
                limitMap.put(limitService.model(),limitService);
            }
        }
    }

    public ILimitService getBean(LimitModelEnum limitModelEnum){
        return limitMap.get(limitModelEnum);
    }
}
