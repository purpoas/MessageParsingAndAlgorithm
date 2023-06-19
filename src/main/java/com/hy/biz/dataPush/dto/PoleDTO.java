package com.hy.biz.dataPush.dto;

import com.hy.domain.Pole;
import lombok.Data;

@Data
public class PoleDTO {

    //最上级主线Id
    private String mainLineId;

    //线路编号
    private String lineId;

    //线路与主线层级
    private Integer lineDepth;

    //杆塔编号
    private String poleId;

    //塔序
    private Integer poleSerial;

    //杆塔与起始变电站距离
    private Double distanceToHeadStation;

    public PoleDTO() {
    }

    public PoleDTO(Pole pole) {
        this.lineId = pole.getOrg() == null ? null : String.valueOf(pole.getOrg().getId());
        this.poleId = String.valueOf(pole.getId());
        this.poleSerial = pole.getOrderNum();
    }

    public PoleDTO(String mainLineId, String lineId, Integer lineDepth, String poleId, Integer poleSerial, Double distanceToHeadStation) {
        this.mainLineId = mainLineId;
        this.lineId = lineId;
        this.lineDepth = lineDepth;
        this.poleId = poleId;
        this.poleSerial = poleSerial;
        this.distanceToHeadStation = distanceToHeadStation;
    }

    public PoleDTO(String mainLineId, String lineId, String poleId, Integer poleSerial, Double distanceToHeadStation) {
        this.mainLineId = mainLineId;
        this.lineId = lineId;
        this.poleId = poleId;
        this.poleSerial = poleSerial;
        this.distanceToHeadStation = distanceToHeadStation;
    }

}
