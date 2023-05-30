package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * 短路故障特征返回类
 */
@Data
public class FeatureShortDTO {

    private int faultPhaseId;   //1-AB相短路, 2-BC相短路，3-CA相短路，4-ABC相短路
    private int protectType;    //1-过流I，2-过流II，3-过流III
    private int areStat;        //重合闸状态，0-重合闸失败，1-重合闸成功
    private double faultCur;  //故障电流，单位A

}
