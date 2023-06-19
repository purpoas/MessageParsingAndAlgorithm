package com.hy.biz.dataAnalysis.dto;

import com.hy.biz.dataAnalysis.featureAlgorithm.FaultFeatureAlgorithm;
import lombok.Data;

/**
 * 合闸涌流特征返回类
 */
@Data
public class FeatureFlowDTO {

    private String Inrush;          //变压器合闸涌流数据
    private String InrushTime;      //涌流时间
    private Double H1;              //基波电流，单位A
    private Double H2;              //二次谐波电流，单位A
    private Double Th2;             //二次谐波含量
    private String type = FaultFeatureAlgorithm.FEATURE_FLOW;   //特征类型 Break-断路 Short-短路 Ground-接地 Flow-合闸涌流 Undulate-负荷波动


    public FeatureFlowDTO(String inrushTime, Double h1, Double h2, Double th2) {
        InrushTime = inrushTime;
        H1 = h1;
        H2 = h2;
        Th2 = th2;
    }
}
