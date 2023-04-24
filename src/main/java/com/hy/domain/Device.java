package com.hy.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "device")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    @NotNull
    @Size(max = 100)
    private String name;

    @Column(name = "code", length = 100, nullable = false)
    @NotNull
    @Size(max = 100)
    private String code;

    /**
     * 目前设备只有一种类型：1-交流型
     */
    @Column(name = "type")
    private Integer type;

    /**
     * 0-无 1-A 2-B 3-C
     */
    @Column(name = "phase")
    private Integer phase;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "remark", columnDefinition = "text")
    private String remark;


    @NotNull
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "updated_by")
    private Long updatedBy;


    /**
     * 安装时间
     */
    @Column(name = "install_time")
    private Instant installTime;
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
    @JsonIgnoreProperties("devices")
    private Pole pole;

    @ManyToMany(mappedBy = "devices")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnore
    @ToString.Exclude
    private Set<User> users = new HashSet<>();

    public Device(Long id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Device device = (Device) o;
        return getId() != null && Objects.equals(getId(), device.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
