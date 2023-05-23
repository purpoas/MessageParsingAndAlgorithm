package com.hy.biz.cache.service;

import com.google.common.cache.CacheBuilder;
import com.hy.biz.cache.bean.BaseCacheManager;
import com.hy.biz.cache.listener.CacheRemovalListener;
import com.hy.biz.dataAnalysis.dto.AlgorithmIdentify;
import com.hy.biz.dataAnalysis.dto.AlgorithmTask;
import com.hy.biz.dataAnalysis.dto.FaultAnalysisResult;
import com.hy.config.HyConfigProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 接地故障缓存管理
 */
@Component
public class GroundFaultCacheManager extends BaseCacheManager<String, FaultAnalysisResult> {

    private final RedisTemplate<String, String> redisTemplate;

    // 是否持久化
    private Boolean persisted;

    public GroundFaultCacheManager(RedisTemplate<String, String> redisTemplate, HyConfigProperty hyConfigProperty) {
        this.cache = CacheBuilder.newBuilder()
                .initialCapacity(100) // 初始化缓存容量
                .concurrencyLevel(4) // 最大并发写入线程数
                .expireAfterAccess(20, TimeUnit.MINUTES) // 缓存内容在访问 5 分钟后失效
                .removalListener(new CacheRemovalListener<>(this))
                .build();
        this.redisTemplate = redisTemplate;
        this.persisted = true;
    }


    @Override
    public void persisted(String key, FaultAnalysisResult value) {

    }
}
