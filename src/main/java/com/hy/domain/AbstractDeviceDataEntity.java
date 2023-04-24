package com.hy.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Created by Dylan on 2023/4/7 15:40.
 *
 * @author Dylan
 */
@Setter
@Getter
@MappedSuperclass
public abstract class AbstractDeviceDataEntity<E extends AbstractCollectionTimeEntity<E>> extends AbstractCollectionTimeEntity<E> {
    /**
     * 设备ID
     */
    @Column(name = "device_id")
    private Long deviceId;

}
