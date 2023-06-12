package com.hy.biz.dataAnalysis.featureAlgorithm;

import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.algorithmUtil.AnalysisConstants;

import java.util.List;

/**
 * 故障特征计算：
 * 1.单向接地故障特征
 * 2.短路故障特性
 * 3.缺相故障特征
 */
public class FaultFeatureUtil {

    public void test(FaultIdentifyDTO faultType, AreaLocateDTO areaLocateDTO, List<FaultWave> faultWaveList) {

        switch (faultType.getFaultType()) {
            case AnalysisConstants.FAULT_NATURE_GROUND_A:
            case AnalysisConstants.FAULT_NATURE_GROUND_B:
            case AnalysisConstants.FAULT_NATURE_GROUND_C:
                // 单向接地故障特征
                int faultPhaseId = FeatureGroundCalculate.faultPhaseId(faultType.getFaultType());
                double zeroSeqCur = FeatureGroundCalculate.zeroSeqCur(faultType);
                int isBreak = FeatureGroundCalculate.isBreak(areaLocateDTO, faultWaveList);


                break;
            case AnalysisConstants.FAULT_NATURE_SHORT_AB:
            case AnalysisConstants.FAULT_NATURE_SHORT_AC:
            case AnalysisConstants.FAULT_NATURE_SHORT_BC:
            case AnalysisConstants.FAULT_NATURE_SHORT_ABC:
                // 短路故障特性判断
                int faultPhaseId2 = FeatureShortCalculate.faultPhaseId(faultType.getFaultType());
                double faultCur = FeatureShortCalculate.faultCur(faultType);
                break;
            case AnalysisConstants.FAULT_NATURE_BREAK_AB:
            case AnalysisConstants.FAULT_NATURE_BREAK_AC:
            case AnalysisConstants.FAULT_NATURE_BREAK_BC:
            case AnalysisConstants.FAULT_NATURE_BREAK_A:
            case AnalysisConstants.FAULT_NATURE_BREAK_B:
            case AnalysisConstants.FAULT_NATURE_BREAK_C:
                // 缺相故障特征判断
                FeatureGroundCalculate.test();
                break;
            default:
                break;
        }

    }


}
