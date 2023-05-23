package com.hy.biz.dataAnalysis.dto;

import com.hy.biz.util.GsonUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


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

    // 起始杆塔到结束杆塔的距离
    private Double intervalDistance;

    // 起始杆塔所属故障波形
    private FaultWave headFaultWave;

    // 结束杆塔所属故障波形
    private FaultWave endFaultWave;

    public AreaLocateDTO(String faultHeadTowerId, String faultEndTowerId, Double faultHeadTowerDistanceToHeadStation, Double faultEndTowerDistanceToHeadStation) {
        this.faultHeadTowerId = faultHeadTowerId;
        this.faultEndTowerId = faultEndTowerId;
        this.faultHeadTowerDistanceToHeadStation = faultHeadTowerDistanceToHeadStation;
        this.faultEndTowerDistanceToHeadStation = faultEndTowerDistanceToHeadStation;
        this.intervalDistance = Math.abs(faultEndTowerDistanceToHeadStation - faultEndTowerDistanceToHeadStation);
    }

    public AreaLocateDTO(String faultHeadTowerId, String faultEndTowerId, Double faultHeadTowerDistanceToHeadStation, Double faultEndTowerDistanceToHeadStation, FaultWave headFaultWave, FaultWave endFaultWave) {
        this.faultHeadTowerId = faultHeadTowerId;
        this.faultEndTowerId = faultEndTowerId;
        this.faultHeadTowerDistanceToHeadStation = faultHeadTowerDistanceToHeadStation;
        this.faultEndTowerDistanceToHeadStation = faultEndTowerDistanceToHeadStation;
        this.intervalDistance = faultEndTowerDistanceToHeadStation == null ? 0D : Math.abs(faultEndTowerDistanceToHeadStation - faultEndTowerDistanceToHeadStation);
        this.headFaultWave = headFaultWave;
        this.endFaultWave = endFaultWave;
    }

    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }
}
