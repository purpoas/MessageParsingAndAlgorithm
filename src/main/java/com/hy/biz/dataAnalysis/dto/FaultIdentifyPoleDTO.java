package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * 故障类型识别 用于计算杆塔DTO
 */
@Data
public class FaultIdentifyPoleDTO {

    private String lineId;
    private String poleId;
    private Double distanceToHeadStation;

    private String faultTime;   // 默认A相工频电流波形起始时间

    // 工频电流三相波形内容
    private double[] aPhaseCurrentData;
    private double[] bPhaseCurrentData;
    private double[] cPhaseCurrentData;

    // 工频电压三相波形内容
    private double[] aPhaseVoltageData;
    private double[] bPhaseVoltageData;
    private double[] cPhaseVoltageData;


    public FaultIdentifyPoleDTO(String lineId, String poleId, Double distanceToHeadStation, double[] aPhaseCurrentData, double[] bPhaseCurrentData, double[] cPhaseCurrentData) {
        this.lineId = lineId;
        this.poleId = poleId;
        this.distanceToHeadStation = distanceToHeadStation;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
    }

    public FaultIdentifyPoleDTO(String lineId, String poleId, Double distanceToHeadStation, double[] aPhaseCurrentData, double[] bPhaseCurrentData, double[] cPhaseCurrentData, double[] aPhaseVoltageData, double[] bPhaseVoltageData, double[] cPhaseVoltageData) {
        this.lineId = lineId;
        this.poleId = poleId;
        this.distanceToHeadStation = distanceToHeadStation;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
        this.aPhaseVoltageData = aPhaseVoltageData;
        this.bPhaseVoltageData = bPhaseVoltageData;
        this.cPhaseVoltageData = cPhaseVoltageData;
    }


}
