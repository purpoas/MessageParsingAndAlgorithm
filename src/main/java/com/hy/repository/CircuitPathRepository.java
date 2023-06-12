package com.hy.repository;

import com.hy.domain.CircuitPath;
import com.hy.domain.DeviceFault;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface CircuitPathRepository extends JpaRepository<CircuitPath, Long> {

    /**
     * 根据线路Id查询所有后代线路 包含自身
     **/
    List<CircuitPath> findByDescendant(Long lineId);

    @Query("select c.ancestor from circuit_path c where c.descendant = :lineId order by c.depth")
    List<Long> findByAllDescendantByLineId(@Param("lineId") Long lineId);


    @Query("select c from circuit_path c where c.descendant = :lineId and c.depth = 1")
    CircuitPath findByDescendantAndDepthFix1(@Param("lineId") Long lineId);
}
