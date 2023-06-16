package com.hy.biz.dataAnalysis.dto;

import com.hy.biz.dataAnalysis.featureAlgorithm.FaultFeatureUtil;
import lombok.Data;

/**
 * 负荷波动特征返回类
 */
@Data
public class FeatureUndulateDTO {

    private String loadVariation;        //负荷变化
    private String variationTime;        //变化时间
    private Double changeCurr;           //负荷改变电流，单位A
    private String type = FaultFeatureUtil.FEATURE_UNDULATE;    //特征类型 Break-断路 Short-短路 Ground-接地 Flow-合闸涌流 Undulate-负荷波动


}
