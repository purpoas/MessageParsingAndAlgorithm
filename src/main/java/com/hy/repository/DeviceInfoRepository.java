package com.hy.repository;

import com.hy.domain.DeviceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DeviceInfoRepository extends JpaRepository<DeviceInfo,Long> {
    Page<DeviceInfo> findByDeviceIdInAndCollectionTimeBetween(List<Long> ids, Instant startTime, Instant endTime, Pageable page);
}
