package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

@Data
public class FaultIdentifyDTO {

    // 故障特征值
    private String faultType;

    // 工频电流三相波形内容
    private double[] aPhaseCurrentData;
    private double[] bPhaseCurrentData;
    private double[] cPhaseCurrentData;

    // 工频电压三相波形内容
    private double[] aPhaseVoltageData;
    private double[] bPhaseVoltageData;
    private double[] cPhaseVoltageData;

    // 是否是上游故障判断结果 默认是
    private boolean isUpstream = true;

    public FaultIdentifyDTO(String faultType) {
        this.faultType = faultType;
    }

    public FaultIdentifyDTO(String faultType, double[] aPhaseCurrentData, double[] bPhaseCurrentData, double[] cPhaseCurrentData) {
        this.faultType = faultType;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
    }

    public FaultIdentifyDTO(String faultType, double[] aPhaseCurrentData, double[] bPhaseCurrentData, double[] cPhaseCurrentData, double[] aPhaseVoltageData, double[] bPhaseVoltageData, double[] cPhaseVoltageData) {
        this.faultType = faultType;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
        this.aPhaseVoltageData = aPhaseVoltageData;
        this.bPhaseVoltageData = bPhaseVoltageData;
        this.cPhaseVoltageData = cPhaseVoltageData;
    }

    public FaultIdentifyDTO(String faultType, double[] aPhaseCurrentData, double[] bPhaseCurrentData, double[] cPhaseCurrentData, boolean isUpstream) {
        this.faultType = faultType;
        this.aPhaseCurrentData = aPhaseCurrentData;
        this.bPhaseCurrentData = bPhaseCurrentData;
        this.cPhaseCurrentData = cPhaseCurrentData;
        this.isUpstream = isUpstream;
    }

    public FaultIdentifyDTO(String faultType, double[] aPhaseCurrentData, double[] bPhaseCurrentData, double[] cPhaseCurrentData, double[] aPhaseVoltageData, double[] bPhaseVoltageData, double[] cPhaseVoltageData, boolean isUpstream) {
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
