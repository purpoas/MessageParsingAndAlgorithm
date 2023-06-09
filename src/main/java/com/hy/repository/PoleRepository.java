package com.hy.repository;

import com.hy.domain.Pole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ============================================================
 * ｜
 * ============================================================
 *
 * @author shiwentao
 * @package com.hy.repository
 * @create 2023/6/7 14:02
 **/
@Repository
public interface PoleRepository extends JpaRepository<Pole, Long> {
    Pole findPoleById(Long poleId);


}
