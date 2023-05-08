package com.hy.repository;

import com.hy.domain.DeviceOnlineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author shiwentao
 * @package com.hy.repository
 * @description
 * @create 2023-05-04 16:40
 **/
@Repository
public interface DeviceOnlineStatusRepository extends JpaRepository<DeviceOnlineStatus, Long> {
}
