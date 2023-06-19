package com.hy.biz.dataAnalysis.dto;

import com.hy.biz.dataAnalysis.featureAlgorithm.FaultFeatureAlgorithm;
import lombok.Data;

/**
 * 接地故障特征返回类
 */
@Data
public class FeatureGroundDTO {

    private Integer faultPhaseId;   //1-A相接地, 2-B相接地，3-C相接地
    private Integer faultType;      //0-瞬时故障，1-永久故障
    private Integer isBreak;        //是否分闸，0-未分闸，1-保护分闸
    private Integer areStat;        //重合闸状态，0-重合闸失败，1-重合闸成功
    private Double zeroSeqCur;  //零序电流值，单位A
    private Integer duration;       //瞬时故障持续时间，单位s
    private String type = FaultFeatureAlgorithm.FEATURE_GROUND;        //特征类型 Break-断路 Short-短路 Ground-接地

    public FeatureGroundDTO(Integer faultPhaseId, Integer faultType, Integer isBreak, Double zeroSeqCur) {
        this.faultPhaseId = faultPhaseId;
        this.faultType = faultType;
        this.isBreak = isBreak;
        this.zeroSeqCur = zeroSeqCur;
    }
}
