package com.hy.biz.dataAnalysis.dto;

import com.hy.biz.dataAnalysis.featureAlgorithm.FaultFeatureUtil;
import lombok.Data;

/**
 * 短路故障特征返回类
 */
@Data
public class FeatureShortDTO {

    private Integer faultPhaseId;   //1-AB相短路, 2-BC相短路，3-CA相短路，4-ABC相短路
    private Integer protectType;    //1-过流I，2-过流II，3-过流III
    private Integer areStat;        //重合闸状态，0-重合闸失败，1-重合闸成功
    private Double faultCur;  //故障电流，单位A
    private String type = FaultFeatureUtil.FEATURE_SHORT;        //特征类型 Break-断路 Short-短路 Ground-接地

    public FeatureShortDTO(Integer faultPhaseId, Integer protectType, Integer areStat, Double faultCur) {
        this.faultPhaseId = faultPhaseId;
        this.protectType = protectType;
        this.areStat = areStat;
        this.faultCur = faultCur;
    }
}
