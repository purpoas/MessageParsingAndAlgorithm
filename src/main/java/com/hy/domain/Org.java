package com.hy.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "org")
@Setter
@Getter
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Org implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100)
    @Column(name = "name",length = 100)
    @NotNull
    private String name;

    @ManyToOne
    @JsonIgnoreProperties("orgs")
    private OrgType type;

    /**
     * 当组织类型为公司时，属性信息Properties内储存地址address、Logo配置principal等信息；
     * 当类型为主线路时，属性信息Properties内储存电压等级Voltage、线路长度Length、起始变电站HeadStation等信息；
     * 当类型为支线路时，属性信息Properties内储存电压等级Voltage、线路长度Length；
     * 主线路和支线路的父级组织Id都是公司id
     */
    @Column(name = "properties",columnDefinition = "text")
    private String properties;

    @ManyToOne
    @JsonIgnoreProperties("orgs")
    private Org parent;

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

    @ManyToMany(mappedBy = "orgs")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnore
    private Set<User> users = new HashSet<>();


}
