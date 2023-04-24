package com.hy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity(name = "work_status")
@Setter
@Getter
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class WorkStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_current")
    private Float lineCurrent;

    @Column(name = "deviceTemperature")
    private Float deviceTemperature;
    /**
     * 采集时间
     */
    @NotNull
    @Column(name = "collection_time", nullable = false)
    private Instant collectionTime;

    @ManyToOne
    @JsonIgnoreProperties("workStatus")
    private Device device;
}
