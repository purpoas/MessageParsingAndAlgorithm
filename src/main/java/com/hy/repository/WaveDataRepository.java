package com.hy.repository;

import com.hy.domain.WaveData;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Dylan on 2023/4/10 9:36.
 *
 * @author Dylan
 */
@Repository
public interface WaveDataRepository extends BaseRepository<WaveData, Long>, JpaSpecificationExecutor<WaveData> {
    WaveData findByCode(String code);
}
