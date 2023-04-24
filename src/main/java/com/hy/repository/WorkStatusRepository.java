package com.hy.repository;

import com.hy.domain.WorkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public interface WorkStatusRepository extends JpaRepository<WorkStatus,Long> {
    Page<WorkStatus> findByDeviceIdInAndCollectionTimeBetween(List<Long> ids, Instant startTime, Instant endTime, Pageable pageable);

    List<WorkStatus> findByDeviceIdInAndCollectionTimeBetween(List<Long> ids, Instant startTime, Instant endTime);

    Slice<WorkStatus> findWorkStatusByDeviceIdInAndCollectionTimeBetween(Set<Long> ids, Instant startTime, Instant endTime, Pageable pageable);
}
