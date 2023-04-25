package com.hy.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Created by Dylan on 2023/4/10 9:20.
 * 26.	波形数据
 * @author Dylan
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wave_data")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class WaveData extends AbstractDeviceDataEntity<WaveData> implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 波形种类 1-行波电流 2-工频电流 3-工频电压
     */
    @Column(name = "type")
    private Integer type;
    /**
     * 波形编号
     */
    @Column(name = "code")
    private String code;
    /**
     * 波形长度
     */
    @Column(name = "length")
    private Long length;

    /**
     * 波形起始时间
     */
    @Size(min = 1, max = 50)
    @Column(name = "head_time", length = 50)
    private String headTime;

    /**
     * 采样率
     */
    @Column(name = "sample_rate")
    private Long sampleRate;

    /**
     * 触发阈值
     */
    @Column(name = "threshold")
    private Integer threshold;

    /**
     * 故障性质：0-其他 1-雷击 2-涌流 3-重合闸 4-扰动 5-分闸
     */
    @Column(name = "rela_flag")
    private Integer relaFlag;

    /**
     * 波形数据
     */
    @Column(name = "data", columnDefinition = "text")
    private String data;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "text")
    private String remark;
}
