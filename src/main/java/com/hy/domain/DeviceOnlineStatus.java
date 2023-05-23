package com.hy.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;

import javax.persistence.*;

import java.io.Serializable;

import static org.hibernate.annotations.CacheConcurrencyStrategy.NONSTRICT_READ_WRITE;

/**
 * @author shiwentao
 * @package com.hy.domain
 * @description
 * @create 2023-05-04 16:30
 **/
@Entity
@Table(name = "device_online_status")
@Getter
@Setter
@NoArgsConstructor
@Cache(usage = NONSTRICT_READ_WRITE)
public class DeviceOnlineStatus extends AbstractDeviceDataEntity<DeviceOnlineStatus> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "status")
    private String status;


}
