package com.hy.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Entity(name = "circuit_path")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CircuitPath implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 父级线路
     */
    @NotNull
    @Column(name = "ancestor", nullable = false)
    private Long ancestor;

    /**
     * 后代线路
     */
    @NotNull
    @Column(name = "descendant", nullable = false)
    private Long descendant;

    /**
     * 祖先与后代间的代差
     */
    @Column(name = "depth")
    private Integer depth;

    /**
     * 后代线路依附于父级线路的杆塔id，只有代差为1时才存在
     */
    @Column(name = "head_pole")
    private Long headPole;

    public CircuitPath(Long ancestor,Long descendant,Integer depth,Long headPole){
        this.ancestor=ancestor;
        this.descendant=descendant;
        this.depth=depth;
        this.headPole=headPole;
    }
}
