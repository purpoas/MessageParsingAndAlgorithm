package com.hy.biz.cache.service;

import com.google.common.cache.CacheBuilder;
import com.hy.biz.cache.bean.BaseCacheManager;
import com.hy.biz.cache.listener.CacheRemovalListener;
import com.hy.biz.dataAnalysis.dto.AlgorithmIdentify;
import com.hy.biz.dataAnalysis.dto.AlgorithmTask;
import com.hy.config.HyConfigProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 算法任务缓存管理
 */
@Component
public class AlgorithmCacheManager extends BaseCacheManager<AlgorithmIdentify, AlgorithmTask> {

    private final RedisTemplate<String, String> redisTemplate;

    // 是否持久化
    private Boolean persisted;

    public AlgorithmCacheManager(RedisTemplate<String, String> redisTemplate, HyConfigProperty hyConfigProperty) {
        this.cache = CacheBuilder.newBuilder()
                .initialCapacity(1000) // 初始化缓存容量
                .concurrencyLevel(10) // 最大并发写入线程数
                .expireAfterAccess(20, TimeUnit.MINUTES) // 缓存内容在访问 5 分钟后失效
                .removalListener(new CacheRemovalListener<>(this))
                .build();
        this.redisTemplate = redisTemplate;
        this.persisted = true;
    }

    /**
     * 读取缓存算法任务唯一标识 缓存有就读取 没有就返回null
     *
     * @param identify 算法任务唯一标识
     * @return 算法任务
     */
    public AlgorithmTask get(AlgorithmIdentify identify) {
        return this.cache.getIfPresent(identify);
    }

    /**
     * 显示插入缓存  如该key之前有缓存则直接覆盖
     **/
    public void put(AlgorithmIdentify identify, AlgorithmTask task) {
        this.cache.put(identify, task);
    }


    @Override
    public void persisted(AlgorithmIdentify key, AlgorithmTask value) {
        // 指定时间内还未进行算法分析 自动过期删除 需记录


    }
}
