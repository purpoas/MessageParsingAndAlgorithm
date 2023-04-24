package com.hy.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2020/8/19.
 *
 * @author WeiQuanfu
 */
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "authority")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Authority implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 权限名称
     */
    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    /**
     * 权限编码，全局唯一
     */
    @Size(max = 100)
    @Column(name = "code", length = 100, unique = true)
    private String code;

    /**
     * 类别
     */
    @Column(name = "type")
    private String type;

    /**
     * 适用正则匹配
     */
    @Column(name = "apply_pattern")
    private Boolean applyPattern = false;

    /**
     * 请求接口
     */
    @Column(name = "uri")
    private String uri;

    /**
     * 请求方法{@link org.springframework.web.bind.annotation.RequestMethod}
     */
    @Column(name = "method")
    private String method;

    @ManyToMany(mappedBy = "authorities",fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

}
