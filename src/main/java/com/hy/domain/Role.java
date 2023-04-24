    package com.hy.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色名称
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "name", length = 100, unique = true)
    private String name;

    /**
     * 隶属的组织Id
     */
    @NotNull
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    /**
     * 管理界面权限控制
     */
    @Column(name = "ui_power", columnDefinition = "text")
    private String uiPower;

    /**
     * 其他属性
     */
    @Column(name = "properties", columnDefinition = "text")
    private String properties;

    /**
     * 描述、备注
     */
    @Column(name = "remark", columnDefinition = "text")
    private String remark;

    /**
     * 该节点是否已被删除
     */
    @NotNull
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 该节点是否有效
     */
    @NotNull
    @Column(name = "enable", nullable = false)
    private Boolean enable = true;

    /**
     * 创建者用户Id
     */
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 最终修改者用户Id
     */
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
    private Instant createdTime = Instant.now();

    /**
     * 修改时间
     */
    @NotNull
    @Column(name = "updated_time", nullable = false)
    private Instant updatedTime = Instant.now();

    /**
     * 删除时间
     */
    @Column(name = "deleted_time")
    private Instant deletedTime;


    /**
     * 用户角色双向绑定
     */
    @ManyToMany(mappedBy = "roles")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    /**
     * 角色与权限的绑定
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "role_authority",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName = "id"))
    private Set<Authority> authorities = new HashSet<>();
}
