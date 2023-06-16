package com.hy.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * Created by Dylan on 2023/4/11 10:31.
 * 17.	故障告警
 *
 * @author Dylan
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "device_alarm")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DeviceAlarm extends AbstractCollectionTimeEntity<DeviceAlarm> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 故障线路Id
     */
    @Column(name = "fault_line_id")
    private Long faultLineId;

    /**
     * 故障杆塔id
     */
    @Column(name = "fault_pole_id")
    private Long faultPoleId;

    /**
     * 故障点与最近杆塔的距离
     */
    private Double distToFaultPole;

    /**
     * 故障起始杆塔id
     */
    @Column(name = "fault_head_pole_id")
    private Long faultHeadPoleId;

    /**
     * 故障终止杆塔Id
     */
    @Column(name = "fault_end_pole_id")
    private Long faultEndPoleId;

    /**
     * 故障时间
     */
    @Column(name = "fault_time")
    private String faultTime;

    /**
     * 故障类型
     * eg :
     * AB两相短路 、 AC两相短路 、 BC两相短路 、三相短路
     * AB两相断路 、 AC两相断路 、 BC两相断路 、A相断路 、 B相断路 、 C相断路
     * A相接地 、 B相接地、 C相接地
     * 正常运行 、 合闸涌流 、 负荷波动
     */
    @Column(name = "fault_type")
    private String faultType;

    /**
     * 故障特征，以json字符串存储
     */
    private String faultFeature;

    /**
     * 故障区间
     * eg： XX主线1号杆塔与YY支线10号杆塔
     */
    @Column(name = "fault_area")
    private String faultArea;

    /**
     * 故障距离
     * eg: 距离XX主线5号杆塔大号侧3611米，在XX#59分支线5号杆塔附近
     */
    @Column(name = "fault_distance")
    private String faultDistance;

    /**
     * 故障波形集合
     */
    @Column(name = "fault_wave_set", columnDefinition = "text")
    private String faultWaveSets;

    /**
     * 是否人工处理
     */
    @Column(name = "is_manual")
    private Boolean isManual;

    /**
     * 人工处理的操作者
     */
    @Column(name = "operator", length = 20)
    private String operator;

    /**
     * 告警是否确认
     * 0-未确认  1-已确认，未推送  2-已确认，已推送
     */
    @Column(name = "is_handled")
    private Short isHandled;

    /**
     * 告警处理时间
     */
    @Column(name = "handled_time")
    private Instant handledTime;

    /**
     * 最后处理告警的用户
     */
    @Column(name = "handled_by")
    private Long handledBy;
}
