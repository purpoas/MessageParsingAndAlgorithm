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

    // 工频电流三相波形内容
    private Double[] aPhaseCurrentData;
    private Double[] bPhaseCurrentData;
    private Double[] cPhaseCurrentData;

    // 工频电压三相波形内容
    private Double[] aPhaseVoltageData;
    private Double[] bPhaseVoltageData;
    private Double[] cPhaseVoltageData;


    public FaultIdentifyPoleDTO(String lineId, String poleId, Double distanceToHeadStation, Double[] aPhaseCurrentData, Double[] bPhaseCurrentData, Double[] cPhaseCurrentData) {
        this.lineId = lineId;
        this.poleId = poleId;
        this.distanceToHeadStation = distanceToHeadStation;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
    }

    public FaultIdentifyPoleDTO(String lineId, String poleId, Double distanceToHeadStation, Double[] aPhaseCurrentData, Double[] bPhaseCurrentData, Double[] cPhaseCurrentData, Double[] aPhaseVoltageData, Double[] bPhaseVoltageData, Double[] cPhaseVoltageData) {
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
