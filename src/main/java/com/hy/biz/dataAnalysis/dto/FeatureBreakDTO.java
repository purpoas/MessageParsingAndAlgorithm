package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * 断路故障特征返回类
 */
@Data
public class FeatureBreakDTO {

    private Integer faultPhaseId;   //1-A相断线, 2-B相断线，3-C相断线
    private Integer isBreak;        //是否分闸，0-未分闸，1-保护分闸
    private Double zeroSeqCur;  //零序电流值，单位A
    private Double negSeqCur;   //负序电流值，单位A
    private Double negSeqVol;   //负序电压值，单位V
    private String type = "Break";        //特征类型 Break-断路 Short-短路 Ground-接地

    public FeatureBreakDTO(int faultPhaseId, int isBreak, double zeroSeqCur, double negSeqCur, double negSeqVol) {
        this.faultPhaseId = faultPhaseId;
        this.isBreak = isBreak;
        this.zeroSeqCur = zeroSeqCur;
        this.negSeqCur = negSeqCur;
        this.negSeqVol = negSeqVol;
    }
}
