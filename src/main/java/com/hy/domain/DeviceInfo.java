package com.hy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;

@Entity(name = "device_info")
@Setter
@Getter
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DeviceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_name")
    private String terminalName;
    @Column(name = "terminal_type")
    private String terminalType;
    @Column(name = "terminal_edition")
    private String terminalEdition;

    @Column(name = "producer")
    private String producer;

    @Column(name = "producer_code")
    private String producerCode;
    /**
     * 生产日期
     */
    @Column(name = "producer_time")
    private String producerTime;
    /**
     * 采集时间
     */
    @Column(name = "collection_time")
    private Instant collectionTime;

    @ManyToOne
    @JsonIgnoreProperties("deviceInfos")
    private Device device;

}
