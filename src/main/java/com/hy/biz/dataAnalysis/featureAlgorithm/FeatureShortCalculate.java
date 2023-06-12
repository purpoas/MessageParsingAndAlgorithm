package com.hy.biz.dataAnalysis.featureAlgorithm;

import com.hy.biz.dataAnalysis.dto.FaultIdentifyDTO;
import com.hy.biz.dataAnalysis.algorithmUtil.AnalysisConstants;

import java.util.ArrayList;
import java.util.List;

public class FeatureShortCalculate {

    /**
     * 故障相位转换
     *
     * @param faultType
     * @return 1-AB相短路, 2-BC相短路，3-CA相短路，4-ABC相短路
     */
    public static int faultPhaseId(String faultType) {
        if (AnalysisConstants.FAULT_NATURE_SHORT_AB.equals(faultType)) {
            return 1;
        } else if (AnalysisConstants.FAULT_NATURE_SHORT_BC.equals(faultType)) {
            return 2;
        } else if (AnalysisConstants.FAULT_NATURE_SHORT_AC.equals(faultType)) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * @param faultType
     * @return
     */
    public static double faultCur(FaultIdentifyDTO faultType) {

        // 如果故障相电流数据为故障区间下游数据则不进行故障电流计算
        if (!faultType.isUpstream()) return 0.0;

        List<Double[]> list = new ArrayList<>();
        list.add(faultType.getAPhaseCurrentData());
        list.add(faultType.getBPhaseCurrentData());
        list.add(faultType.getCPhaseCurrentData());

        return 0.0;
    }


}
