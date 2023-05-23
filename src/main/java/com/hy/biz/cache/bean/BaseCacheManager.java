package com.hy.biz.cache.bean;

import com.google.common.cache.Cache;

import java.util.Map;

/**
 * 基础缓存管理器
 * 实现了一些基本的缓存操作方法
 */
public abstract class BaseCacheManager<K,V> implements ICacheManager<K,V> {
    protected Cache<K,V> cache;

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void putAll(Map<K, V> values) {
        cache.putAll(values);
    }

    @Override
    public V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public Map getAll() {
        return cache.asMap();
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public boolean contains(K key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public long size() {
        return cache.size();
    }
}
