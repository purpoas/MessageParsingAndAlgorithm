package com.hy.repository;

import com.hy.domain.Org;
import com.hy.domain.Pole;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgRepository extends JpaRepository<Org, Long> {

    Org findByIdAndDeletedFalse(Long lineId);



}
