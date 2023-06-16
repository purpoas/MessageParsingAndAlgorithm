package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * 故障分析结果
 */
@Data
public class FaultAnalysisResultDTO {

    /**
     * 序列号 - 用于标识所属故障
     */
    private Long serial;

    /**
     * 故障线路Id
     */
    private String faultLineId;

    /**
     * 故障时间
     */
    private String faultTime;

    /**
     * 距离和故障最近的杆塔号
     */
    private String nearestPoleId;

    /**
     * 故障点与最近杆塔的距离
     */
    private Double distToFaultPole;

    /**
     * 故障起始杆塔id
     */
    private String faultHeadPoleId;

    /**
     * 故障终止杆塔Id
     */
    private String faultEndPoleId;

    /**
     * 故障类型
     * eg :
     * AB两相短路 、 AC两相短路 、 BC两相短路 、三相短路
     * AB两相断路 、 AC两相断路 、 BC两相断路 、A相断路 、 B相断路 、 C相断路
     * A相接地 、 B相接地、 C相接地
     * 正常运行 、 合闸涌流 、 负荷波动
     */
    private String faultType;

    /**
     * 故障特征，以json字符串存储
     */
    private String faultFeature;

    /**
     * 故障波形集合，以波形编号存储
     */
    private String faultWaveSets;

}
