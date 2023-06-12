package com.hy.biz.dataAnalysis.featureAlgorithm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hy.biz.dataAnalysis.dto.*;
import com.hy.biz.util.GsonUtil;
import com.hy.config.AnalysisConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 故障特征计算：
 * 1.单向接地故障特征
 * 2.短路故障特性
 * 3.缺相故障特征
 */
public class FaultFeatureUtil {

    public static JsonObject calculate(FaultIdentifyDTO faultType, AreaLocateDTO areaLocateDTO, Set<FaultWave> faultWaveSet) {

        List<FaultWave> faultWaveList = new ArrayList<>(faultWaveSet);

        JsonObject result = null;

        switch (faultType.getFaultType()) {
            case AnalysisConstants.FAULT_NATURE_GROUND_A:
            case AnalysisConstants.FAULT_NATURE_GROUND_B:
            case AnalysisConstants.FAULT_NATURE_GROUND_C:
                // 单向接地故障特征
                int groundPhaseId = FeatureGroundCalculate.faultPhaseId(faultType.getFaultType());
                Integer groundIsBreak = FeatureGroundCalculate.isBreak(areaLocateDTO, faultWaveList);
                double groundZeroSeqCur = FeatureGroundCalculate.zeroSeqCur(faultType);

                FeatureGroundDTO groundDTO = new FeatureGroundDTO(groundPhaseId, groundIsBreak, groundZeroSeqCur);

                result = JsonParser.parseString(GsonUtil.getInstance().toJson(groundDTO)).getAsJsonObject();

                break;
            case AnalysisConstants.FAULT_NATURE_SHORT_AB:
            case AnalysisConstants.FAULT_NATURE_SHORT_AC:
            case AnalysisConstants.FAULT_NATURE_SHORT_BC:
            case AnalysisConstants.FAULT_NATURE_SHORT_ABC:
                // 短路故障特性判断
                int shortPhaseId = FeatureShortCalculate.faultPhaseId(faultType.getFaultType());
                Double faultCur = FeatureShortCalculate.faultCur(faultType);
                Integer protectType = FeatureShortCalculate.protectType(faultType, faultCur, null, null);
                Integer areStat = FeatureShortCalculate.areStat(faultWaveList);

                FeatureShortDTO shortDTO = new FeatureShortDTO(shortPhaseId, protectType, areStat, faultCur);

                result = JsonParser.parseString(GsonUtil.getInstance().toJson(shortDTO)).getAsJsonObject();

                break;
            case AnalysisConstants.FAULT_NATURE_BREAK_AB:
            case AnalysisConstants.FAULT_NATURE_BREAK_AC:
            case AnalysisConstants.FAULT_NATURE_BREAK_BC:
            case AnalysisConstants.FAULT_NATURE_BREAK_A:
            case AnalysisConstants.FAULT_NATURE_BREAK_B:
            case AnalysisConstants.FAULT_NATURE_BREAK_C:
                // 缺相故障特征判断
                int breakPhaseId = FeatureBreakCalculate.faultPhaseId(faultType.getFaultType());
                int breakIsBreak = FeatureBreakCalculate.isBreak(faultType, areaLocateDTO, faultWaveList);
                double breakZeroSeqCur = FeatureBreakCalculate.zeroSeqCur(faultType);
                double negSeqCur = FeatureBreakCalculate.negSeq(faultType.getAPhaseCurrentData(), faultType.getBPhaseCurrentData(), faultType.getCPhaseCurrentData());
                double negSeqVol = FeatureBreakCalculate.negSeq(faultType.getAPhaseVoltageData(), faultType.getBPhaseVoltageData(), faultType.getCPhaseVoltageData());

                FeatureBreakDTO breakDTO = new FeatureBreakDTO(breakPhaseId, breakIsBreak, breakZeroSeqCur, negSeqCur, negSeqVol);

                result = JsonParser.parseString(GsonUtil.getInstance().toJson(breakDTO)).getAsJsonObject();

                break;
            default:
                break;
        }

        return result;
    }


}
