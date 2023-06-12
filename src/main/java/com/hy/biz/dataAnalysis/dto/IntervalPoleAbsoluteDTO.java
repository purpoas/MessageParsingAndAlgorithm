package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * 区间定位 用于计算杆塔DTO
 */
@Data
public class IntervalPoleAbsoluteDTO {

    private String lineId;
    private String poleId;
    private Double distanceToHeadStation;
    private Boolean absolute;
    private double[] data;

    public IntervalPoleAbsoluteDTO() {
    }

    public IntervalPoleAbsoluteDTO(String lineId, String poleId, Double distanceToHeadStation, Boolean absolute) {
        this.lineId = lineId;
        this.poleId = poleId;
        this.distanceToHeadStation = distanceToHeadStation;
        this.absolute = absolute;
    }
}
