package com.hy.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;

/**
 * Created by Dylan on 2023/4/7 15:41.
 *
 * @author Dylan
 */
@Setter
@Getter
@MappedSuperclass
public abstract class AbstractCollectionTimeEntity<E extends AbstractCollectionTimeEntity<E>> implements Serializable, Comparable<E> {

    /**
     * 采集时间
     */
    @Column(name = "collection_time")
    private Instant collectionTime;

    /**
     * 采集时间 CollectionTime 的比较方法
     *
     * @param other 进行比较的另一个对象
     * @return  -1 : 采集时间早于被比较对象的时间;
     *           0 : 采集时间等于被比较对象的时间;
     *           1 : 采集时间晚于被比较对象的时间
     */
    @Override
    public int compareTo(E other) {
        if (getCollectionTime().isBefore(other.getCollectionTime())) {
            return -1;
        } else if (getCollectionTime().equals(other.getCollectionTime())) {
            return 0;
        } else {
            return 1;
        }
    }
}