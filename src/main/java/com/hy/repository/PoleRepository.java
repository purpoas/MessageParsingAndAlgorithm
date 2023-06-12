package com.hy.repository;

import com.hy.domain.CircuitPath;
import com.hy.domain.Pole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoleRepository extends JpaRepository<Pole, Long> {

    Pole findPoleById(Long poleId);

    @Query(value = "SELECT sum(distance_to_last_pole) from pole WHERE org_id = :orgId and order_num <= :orderNum", nativeQuery = true)
    Double calculatePoleDistanceToHeadStation(@Param("orgId") Long orgId, @Param("orderNum") Integer orderNum);

    Pole findByIdAndDeletedFalse(Long poleId);

    List<Pole> findByOrgIdAndDeletedFalse(Long lineId);

}
