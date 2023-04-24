package com.hy.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 真实姓名
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 登陆账号
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "login_id", length = 100)
    private String loginId;

    /**
     * 密码
     */
    @Column(name = "password")
    private String password;

    /**
     * 手机号
     */
    @Size(max = 20)
    @Column(name = "cellphone", length = 20)
    private String cellPhone;

    /**
     * 其他属性
     */
    @Column(name = "properties", columnDefinition = "text")
    //@Type(type = "JsonbType")
    //@JsonField
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
    private Boolean deleted;

    /**
     * 该节点是否有效
     */
    @NotNull
    @Column(name = "enable", nullable = false)
    private Boolean enable;

    /**
     * 创建者用户Id
     */
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 最终修改者用户Id
     */
    @NotNull
    @Column(name = "updated_by", nullable = false)
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

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "user_org",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "org_id", referencedColumnName = "id"))
    private Set<Org> orgs = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "user_device",
            joinColumns =@JoinColumn(name = "user_id",referencedColumnName = "id"),
            inverseJoinColumns =@JoinColumn(name = "device_id",referencedColumnName = "id"))
    private Set<Device> devices=new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;


}
