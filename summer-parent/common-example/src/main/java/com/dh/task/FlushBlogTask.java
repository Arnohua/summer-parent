package com.dh.task;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dinghua
 * @date 2019/11/22
 * @since v1.0.0
 */

@EnableScheduling
@Component
public class FlushBlogTask {

    private RestTemplate restTemplate;

    private Set<String> urls = new HashSet<>();

    @PostConstruct
    public void init(){
        restTemplate = new RestTemplate();
        urls.add("https://blog.csdn.net/ding_hua/article/details/103189775");
        urls.add("https://blog.csdn.net/ding_hua/article/details/86133588");
        urls.add("https://blog.csdn.net/ding_hua/article/details/85084127");
        urls.add("https://blog.csdn.net/ding_hua/article/details/84992817");
        urls.add("https://blog.csdn.net/ding_hua/article/details/84990614");
        urls.add("https://blog.csdn.net/ding_hua/article/details/84963017");
        urls.add("https://blog.csdn.net/ding_hua/article/details/84951437");
        urls.add("https://blog.csdn.net/ding_hua/article/details/84928906");
    }

    @Scheduled(cron = "0 0/5 * * * ? ")
    public void flush(){
        System.out.println("任务执行...........");
        for(String url : urls){
            ResponseEntity forObject = restTemplate.getForEntity(url, null);
            System.out.println(forObject.getStatusCode());
            restTemplate.put(url,null);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
