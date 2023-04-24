package com.hy.repository;

import com.hy.domain.DeviceFault;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;

@Repository
public interface DeviceFaultRepository extends JpaRepository<DeviceFault, Long>, JpaSpecificationExecutor<DeviceFault> {
    Page<DeviceFault> findDeviceFaultByDeviceIdInAndCollectionTimeBetween(Collection<Long> deviceIds, Instant startTime, Instant endTime, Pageable pageable);

    Slice<DeviceFault> findByDeviceIdInAndCollectionTimeBetween(Collection<Long> deviceIds, Instant startTime, Instant endTime, Pageable pageable);

}
