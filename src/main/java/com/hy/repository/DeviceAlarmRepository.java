package com.hy.repository;

import com.hy.domain.DeviceAlarm;
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
public interface DeviceAlarmRepository extends JpaRepository<DeviceAlarm, Long>, JpaSpecificationExecutor<DeviceAlarm> {



}
