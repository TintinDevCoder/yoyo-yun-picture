package com.dd.yunpicturebackend.schedule;

import com.dd.yunpicturebackend.manager.CacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling

@Slf4j
public class CacheCleanTask {
    @Resource
    private CacheManager cacheManager;

    // 刷新图片分页展示的缓存
    @Scheduled(fixedRate = 6000) // 每分钟执行一次
    public void deleteCache() {
        String key = "yunpicture:listPictureVOByPage";
        // 删除缓存
        cacheManager.refreshAllCacheByPrefix(key);
    }
}
