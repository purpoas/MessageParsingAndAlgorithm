package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * 接地故障特征返回类
 */
@Data
public class FeatureGroundDTO {

    private int faultPhaseId;   //1-A相接地, 2-B相接地，3-C相接地
    private int faultType;      //0-瞬时故障，1-永久故障
    private int isBreak;        //是否分闸，0-未分闸，1-保护分闸
    private int areStat;        //重合闸状态，0-重合闸失败，1-重合闸成功
    private double zeroSeqCur;  //零序电流值，单位A
    private int duration;       //瞬时故障持续时间，单位s

}
