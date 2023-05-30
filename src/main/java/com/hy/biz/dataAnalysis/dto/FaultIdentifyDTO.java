package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

@Data
public class FaultIdentifyDTO {

    // 故障特征值
    private String faultType;

    // 工频电流三相波形内容
    private Double[] aPhaseCurrentData;
    private Double[] bPhaseCurrentData;
    private Double[] cPhaseCurrentData;

    // 工频电压三相波形内容
    private Double[] aPhaseVoltageData;
    private Double[] bPhaseVoltageData;
    private Double[] cPhaseVoltageData;

    // 是否是上游故障判断结果 默认是
    private boolean isUpstream = true;

    public FaultIdentifyDTO(String faultType) {
        this.faultType = faultType;
    }

    public FaultIdentifyDTO(String faultType, Double[] aPhaseCurrentData, Double[] bPhaseCurrentData, Double[] cPhaseCurrentData) {
        this.faultType = faultType;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
    }

    public FaultIdentifyDTO(String faultType, Double[] aPhaseCurrentData, Double[] bPhaseCurrentData, Double[] cPhaseCurrentData, Double[] aPhaseVoltageData, Double[] bPhaseVoltageData, Double[] cPhaseVoltageData) {
        this.faultType = faultType;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
        this.aPhaseVoltageData = aPhaseVoltageData;
        this.bPhaseVoltageData = bPhaseVoltageData;
        this.cPhaseVoltageData = cPhaseVoltageData;
    }

    public FaultIdentifyDTO(String faultType, Double[] aPhaseCurrentData, Double[] bPhaseCurrentData, Double[] cPhaseCurrentData, boolean isUpstream) {
        this.faultType = faultType;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
        this.isUpstream = isUpstream;
    }

    public FaultIdentifyDTO(String faultType, Double[] aPhaseCurrentData, Double[] bPhaseCurrentData, Double[] cPhaseCurrentData, Double[] aPhaseVoltageData, Double[] bPhaseVoltageData, Double[] cPhaseVoltageData, boolean isUpstream) {
        this.faultType = faultType;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
        this.aPhaseVoltageData = aPhaseVoltageData;
        this.bPhaseVoltageData = bPhaseVoltageData;
        this.cPhaseVoltageData = cPhaseVoltageData;
        this.isUpstream = isUpstream;
    }
}
