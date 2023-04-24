package com.hy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

@Entity(name = "pole")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Pole implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",length = 100,nullable = false)
    @NotNull
    @Size(max = 100)
    private String name;

    @Column(name = "order_num")
    private Integer orderNum;


    @Column(name = "distance_to_last_pole")
    private Double distanceToLastPole;


    /**
     * 1-直线 2-耐张 3-电缆
     */
    @Column(name = "type")
    private Integer type;

    @Column(name = "latitude")
    private Float latitude;

    @Column(name = "longitude")
    private Float longitude;

    @Column(name = "altitude")
    private Float altitude;

    @Column(name = "remark",columnDefinition = "text")
    private String remark;


    @NotNull
    @Column(name = "deleted",nullable = false)
    private Boolean deleted;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 删除者用户Id
     */
    @Column(name = "delete_by")
    private Long deleteBy;

    /**
     * 创建时间
     */
    @NotNull
    @Column(name = "created_time", nullable = false)
    private Instant createdTime;

    /**
     * 修改时间
     */
    @NotNull
    @Column(name = "updated_time", nullable = false)
    private Instant updatedTime;

    /**
     * 删除时间
     */
    @Column(name = "deleted_time")
    private Instant deletedTime;

    @ManyToOne
    @JsonIgnoreProperties("poles")
    private Org org;
}
