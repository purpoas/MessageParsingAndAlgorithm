package com.hy.biz.dataPush.dto;

import lombok.Data;

@Data
public class PoleDTO {

    //线路长度
    private Double lineLength;
    //杆塔与起始变电站距离
    private Double distanceToHeadStation;

    //线路编号
    private String lineId;

    //杆塔编号
    private String poleId;

}
