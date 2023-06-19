package com.hy.biz.dataAnalysis.dto;

import com.hy.biz.util.GsonUtil;
import lombok.Data;


/**
 * 区间定位DTO
 */
@Data
public class AreaLocateDTO {

    // 起始杆塔
    private String faultHeadTowerId;

    // 结束杆塔
    private String faultEndTowerId;

    // 起始杆塔到变电站距离
    private Double faultHeadTowerDistanceToHeadStation;

    // 结束杆塔到变电站距离
    private Double faultEndTowerDistanceToHeadStation;

    // 极性
    private Boolean headFaultWaveAbsolute;
    private Boolean endFaultWaveAbsolute;

    // 电流值
    private double[] headFaultWaveData;
    private double[] endFaultWaveData;

    public AreaLocateDTO(String faultHeadTowerId, String faultEndTowerId, Double faultHeadTowerDistanceToHeadStation, Double faultEndTowerDistanceToHeadStation) {
        this.faultHeadTowerId = faultHeadTowerId;
        this.faultEndTowerId = faultEndTowerId;
        this.faultHeadTowerDistanceToHeadStation = faultHeadTowerDistanceToHeadStation;
        this.faultEndTowerDistanceToHeadStation = faultEndTowerDistanceToHeadStation;
    }

    public AreaLocateDTO(String faultHeadTowerId, String faultEndTowerId, Double faultHeadTowerDistanceToHeadStation, Double faultEndTowerDistanceToHeadStation, Boolean headFaultWaveAbsolute, Boolean endFaultWaveAbsolute, double[] headFaultWaveData, double[] endFaultWaveData) {
        this.faultHeadTowerId = faultHeadTowerId;
        this.faultEndTowerId = faultEndTowerId;
        this.faultHeadTowerDistanceToHeadStation = faultHeadTowerDistanceToHeadStation;
        this.faultEndTowerDistanceToHeadStation = faultEndTowerDistanceToHeadStation;
        this.headFaultWaveAbsolute = headFaultWaveAbsolute;
        this.endFaultWaveAbsolute = endFaultWaveAbsolute;
        this.headFaultWaveData = headFaultWaveData;
        this.endFaultWaveData = endFaultWaveData;
    }

    public AreaLocateDTO(String faultHeadTowerId, String faultEndTowerId, Double faultHeadTowerDistanceToHeadStation, Double faultEndTowerDistanceToHeadStation, Boolean headFaultWaveAbsolute, Boolean endFaultWaveAbsolute) {
        this.faultHeadTowerId = faultHeadTowerId;
        this.faultEndTowerId = faultEndTowerId;
        this.faultHeadTowerDistanceToHeadStation = faultHeadTowerDistanceToHeadStation;
        this.faultEndTowerDistanceToHeadStation = faultEndTowerDistanceToHeadStation;
        this.headFaultWaveAbsolute = headFaultWaveAbsolute;
        this.endFaultWaveAbsolute = endFaultWaveAbsolute;
    }

    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }
}
