package com.hy.biz.cache.listener;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.hy.biz.cache.bean.ICacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于处理缓存中数据删除的处理
 * 可根据数据删除原因(过期、手动删除、替换、超出容量上限、被GC回收)对要删除的数据进行不同处理
 */
public class CacheRemovalListener<K,V> implements RemovalListener<K,V> {
    private final Logger log = LoggerFactory.getLogger(CacheRemovalListener.class);
    private ICacheManager<K,V> cacheManager;

    public CacheRemovalListener(ICacheManager<K,V> cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void onRemoval(RemovalNotification<K,V> notification) {
        // 按删除方式处理
        switch (notification.getCause()){
            case REPLACED:
                // 替换
                 break;
            case EXPIRED:
                // 过期
                this.cacheManager.persisted(notification.getKey(),notification.getValue());
                break;
            case EXPLICIT:
                break;
            case SIZE:
                // 超出容量上限
                break;
            case COLLECTED:
                // 被垃圾回收
                break;
            default:
        }
    }
}
