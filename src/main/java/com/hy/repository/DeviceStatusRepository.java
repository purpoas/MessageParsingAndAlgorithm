package com.hy.repository;

import com.hy.domain.DeviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Collection;

/**
 * Created by Dylan on 2023/4/7 15:51.
 *
 * @author Dylan
 */
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long>, JpaSpecificationExecutor<DeviceStatus> {

    Slice<DeviceStatus> findByDeviceIdInAndCollectionTimeBetween(Collection<Long> deviceIds, Instant startTime, Instant endTime, Pageable pageable);

    Page<DeviceStatus> findDeviceStatusByDeviceIdInAndCollectionTimeBetween(Collection<Long> deviceIds, Instant startTime, Instant endTime, Pageable pageable);
}
