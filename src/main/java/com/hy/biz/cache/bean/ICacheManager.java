package com.hy.biz.cache.bean;

import java.util.Map;

/**
 * 缓存管理器
 * 定义了常用的必须的方法
 * K 缓存使用的键的类型
 * V 缓存对象的类型
 */
public interface ICacheManager<K, V> {
    /**
     * 添加缓存
     *
     * @param key   键
     * @param value 值
     */
    void put(K key, V value);

    /**
     * 批量添加
     *
     * @param values 键值对的 map
     */
    void putAll(Map<K, V> values);

    /**
     * 获取缓存值
     *
     * @param key 键
     * @return 值
     */
    V getIfPresent(K key);

    /**
     * 获取所有
     *
     * @return 缓存中所有的键值对map
     */
    Map getAll();

    /**
     * 按键删除值
     *
     * @param key 键
     */
    void remove(K key);

    /**
     * 删除所有记录
     */
    void clear();

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 存在与否
     */
    boolean contains(K key);

    /**
     * 缓存数量
     *
     * @return 缓存数量
     */
    long size();

    /**
     * 持久化处理
     *
     * @param key   键
     * @param value 值
     */
    void persisted(K key, V value);


}
