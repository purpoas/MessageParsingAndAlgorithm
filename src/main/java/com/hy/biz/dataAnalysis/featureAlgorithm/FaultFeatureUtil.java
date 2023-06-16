package com.hy.biz.dataAnalysis.featureAlgorithm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hy.biz.dataAnalysis.algorithmUtil.AnalysisConstants;
import com.hy.biz.dataAnalysis.dto.*;
import com.hy.biz.util.GsonUtil;
import org.springframework.util.StringUtils;

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

    public static final String FEATURE_GROUND = "Ground";
    public static final String FEATURE_SHORT = "Short";
    public static final String FEATURE_BREAK = "Break";
    public static final String FEATURE_FLOW = "Flow";
    public static final String FEATURE_UNDULATE = "Undulate";


    public static String createFaultFeatureDescription(String faultFeature) {

        JsonObject faultFeatureObj = JsonParser.parseString(faultFeature).getAsJsonObject();

        String faultType = faultFeatureObj.get("type").getAsString();

        StringBuilder sb = new StringBuilder();
        switch (faultType) {
            case FEATURE_BREAK:

                FeatureBreakDTO breakDTO = GsonUtil.getInstance().fromJson(faultFeature, FeatureBreakDTO.class);

                if (breakDTO.getIsBreak() != null) {
                    sb.append(breakDTO.getIsBreak() == 0 ? "未分闸" : "保护分闸").append(";");
                }
                if (breakDTO.getZeroSeqCur() != null) {
                    sb.append("零流").append(breakDTO.getZeroSeqCur()).append("A;");
                }
                if (breakDTO.getNegSeqCur() != null) {
                    sb.append("负流").append(breakDTO.getNegSeqCur()).append("A;");
                }
                if (breakDTO.getNegSeqVol() != null) {
                    sb.append("负压").append(breakDTO.getNegSeqVol()).append("V;");
                }

                break;
            case FEATURE_GROUND:

                FeatureGroundDTO groundDTO = GsonUtil.getInstance().fromJson(faultFeature, FeatureGroundDTO.class);

                if (groundDTO.getFaultType() != null) {
                    sb.append(groundDTO.getFaultType() == 0 ? "瞬时故障" : "永久故障").append("A;");
                }
                if (groundDTO.getIsBreak() != null) {
                    sb.append(groundDTO.getIsBreak() == 0 ? "未分闸" : "保护分闸").append(";");
                }
                if (groundDTO.getAreStat() != null) {
                    sb.append(groundDTO.getAreStat() == 0 ? "重合闸失败" : "重合闸成功").append(";");
                }
                if (groundDTO.getZeroSeqCur() != null) {
                    sb.append("零流").append(groundDTO.getZeroSeqCur()).append("A;");
                }
                if (groundDTO.getDuration() != null) {
                    sb.append("持续").append(groundDTO.getDuration()).append("秒;");
                }

                break;
            case FEATURE_SHORT:

                FeatureShortDTO shortDTO = GsonUtil.getInstance().fromJson(faultFeature, FeatureShortDTO.class);

                if (shortDTO.getProtectType() != null) {
                    if (shortDTO.getProtectType() == 1) {
                        sb.append("过流I").append(";");
                    } else if (shortDTO.getProtectType() == 2) {
                        sb.append("过流II").append(";");
                    } else if (shortDTO.getProtectType() == 3) {
                        sb.append("过流III").append(";");
                    } else {
                        sb.append("过流未知").append(";");
                    }
                }
                if (shortDTO.getAreStat() != null) {
                    sb.append(shortDTO.getAreStat() == 0 ? "重合闸失败" : "重合闸成功").append(";");
                }
                if (shortDTO.getFaultCur() != null) {
                    sb.append("故障电流").append(shortDTO.getFaultCur()).append("A;");
                }

                break;
            case FEATURE_FLOW:

                FeatureFlowDTO flowDTO = GsonUtil.getInstance().fromJson(faultFeature, FeatureFlowDTO.class);

                if (StringUtils.hasText(flowDTO.getInrush())) {
                    sb.append(flowDTO.getInrush()).append(";");
                }
                if (flowDTO.getInrushTime() != null) {
                    sb.append("涌流时间").append(flowDTO.getInrushTime()).append(";");
                }
                if (flowDTO.getH1() != null) {
                    sb.append("基波电流").append(flowDTO.getH1()).append("A;");
                }
                if (flowDTO.getH2() != null) {
                    sb.append("二次谐波电流").append(flowDTO.getH2()).append("A;");
                }
                if (flowDTO.getTh2() != null) {
                    sb.append("二次谐波含量").append(flowDTO.getTh2()).append(";");
                }

                break;
            case FEATURE_UNDULATE:

                FeatureUndulateDTO undulateDTO = GsonUtil.getInstance().fromJson(faultFeature, FeatureUndulateDTO.class);

                if (StringUtils.hasText(undulateDTO.getLoadVariation())) {
                    sb.append(undulateDTO.getLoadVariation()).append(";");
                }
                if (undulateDTO.getVariationTime() != null) {
                    sb.append("变化时间").append(undulateDTO.getVariationTime()).append(";");
                }
                if (undulateDTO.getChangeCurr() != null) {
                    sb.append("负荷改变电流").append(undulateDTO.getChangeCurr()).append("A;");
                }

                break;
            default:
                sb.append("未知故障");
                break;
        }


        return sb.toString();
    }


    public static String calculate(FaultIdentifyDTO faultType, AreaLocateDTO areaLocateDTO, Set<FaultWave> faultWaveSet) {

        List<FaultWave> faultWaveList = new ArrayList<>(faultWaveSet);

        String result = null;

        switch (faultType.getFaultType()) {
            case AnalysisConstants.FAULT_NATURE_GROUND_A:
            case AnalysisConstants.FAULT_NATURE_GROUND_B:
            case AnalysisConstants.FAULT_NATURE_GROUND_C:
                // 单向接地故障特征
                int groundPhaseId = FeatureGroundCalculate.faultPhaseId(faultType.getFaultType());
                Integer groundIsBreak = FeatureGroundCalculate.isBreak(areaLocateDTO, faultWaveList);
                double groundZeroSeqCur = FeatureGroundCalculate.zeroSeqCur(faultType);
                Integer groundFaultType = FeatureGroundCalculate.groundFaultType(faultWaveSet);

                FeatureGroundDTO groundDTO = new FeatureGroundDTO(groundPhaseId, groundIsBreak, groundZeroSeqCur);

                result = GsonUtil.getInstance().toJson(groundDTO);

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

                result = GsonUtil.getInstance().toJson(shortDTO);

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

                result = GsonUtil.getInstance().toJson(breakDTO);

                break;
            default:
                break;
        }

        return result;
    }


}
