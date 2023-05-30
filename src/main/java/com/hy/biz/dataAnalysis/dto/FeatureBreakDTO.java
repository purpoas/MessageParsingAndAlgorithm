package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * 断路故障特征返回类
 */
@Data
public class FeatureBreakDTO {

    private int faultPhaseId;   //1-A相断线, 2-B相断线，3-C相断线
    private int isBreak;        //是否分闸，0-未分闸，1-保护分闸
    private double zeroSeqCur;  //零序电流值，单位A
    private double negSeqCur;   //负序电流值，单位A
    private double negSeqVol;   //负序电压值，单位V

}
