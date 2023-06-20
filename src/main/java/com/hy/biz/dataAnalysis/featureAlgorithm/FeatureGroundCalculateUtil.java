package com.hy.biz.dataAnalysis.featureAlgorithm;

import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.*;
import com.hy.biz.dataAnalysis.typeAlgorithm.FrequencyCharacterCalculateUtil;
import com.hy.config.AnalysisConstants;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class FeatureGroundCalculateUtil {

    public static FeatureGroundDTO test() {


        return null;
    }

    /**
     * 故障相位转换
     *
     * @param faultType
     * @return 1-A相接地, 2-B相接地，3-C相接地
     */
    public static int faultPhaseId(String faultType) {
        if (AnalysisConstants.FAULT_NATURE_GROUND_A.equals(faultType)) {
            return 1;
        } else if (AnalysisConstants.FAULT_NATURE_GROUND_B.equals(faultType)) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * 判断接地故障是否是瞬时故障
     *
     * @param faultWaveSet
     * @return 0-瞬时故障，1-永久故障
     */
    public static Integer groundFaultType(Set<FaultWave> faultWaveSet) {


        List<FaultWave> faultWaveList = new ArrayList<>(faultWaveSet);

        // 找三相电流杆塔
        List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(faultWaveList);

        List<String> poleIds = threePhaseCurrents.stream().map(FaultIdentifyPoleDTO::getPoleId).distinct().collect(Collectors.toList());
        // 找三相电流三相电压杆塔
        List<FaultIdentifyPoleDTO> threePhaseVoltages = CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(poleIds, faultWaveList);

        for (FaultIdentifyPoleDTO faultIdentify : threePhaseVoltages) {

            double UAJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getAPhaseVoltageData());
            double UBJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getBPhaseVoltageData());
            double UCJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getCPhaseVoltageData());

            double[] U0 = FrequencyCharacterCalculateUtil.synthesisZeroCurrent(faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
            double U0JMP = FrequencyCharacterCalculateUtil.UJMP(U0);

            if (UAJMP > 0 && UBJMP < 0 && UCJMP < 0 && U0JMP < 0) {
                return 0;
            } else if (UAJMP < 0 && UBJMP > 0 && UCJMP < 0 && U0JMP < 0) {
                return 0;
            } else if (UAJMP < 0 && UBJMP < 0 && UCJMP > 0 && U0JMP < 0) {
                return 0;
            }
        }
        return null;
    }

    /**
     * 零序电流特征计算
     *
     * @param faultIdentifyDTO
     * @return
     */
    public static double zeroSeqCur(FaultIdentifyDTO faultIdentifyDTO) {

        double[] i0 = FrequencyCharacterCalculateUtil.synthesisZeroCurrent(faultIdentifyDTO.getAPhaseCurrentData(), faultIdentifyDTO.getBPhaseCurrentData(), faultIdentifyDTO.getCPhaseCurrentData());

        double zero = FrequencyCharacterCalculateUtil.calculateCyclicWavePH(i0, 5, AnalysisConstants.CYCLE_WAVE_LENGTH);

        return zero / 3;
    }

    /**
     * 分闸状态分析
     *
     * @param areaLocateDTO
     * @param faultWaveList
     * @return 0-未分闸，1-保护分闸 默认无结果返回0
     */
    public static Integer isBreak(AreaLocateDTO areaLocateDTO, List<FaultWave> faultWaveList) {

        if (!StringUtils.hasText(areaLocateDTO.getFaultHeadTowerId())) return null;

        List<FaultWave> leftWave = faultWaveList.stream().filter(faultWave -> faultWave.getDistanceToHeadStation() < areaLocateDTO.getFaultHeadTowerDistanceToHeadStation()).collect(Collectors.toList());

        // 找三相电流杆塔
        List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(leftWave);

        List<String> poleIds = threePhaseCurrents.stream().map(FaultIdentifyPoleDTO::getPoleId).distinct().collect(Collectors.toList());
        // 找三相电流三相电压杆塔
        List<FaultIdentifyPoleDTO> threePhaseVoltages = CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(poleIds, leftWave);

        for (FaultIdentifyPoleDTO faultIdentify : threePhaseVoltages) {
            double IAJMP = FrequencyCharacterCalculateUtil.IJMP(faultIdentify.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterCalculateUtil.IJMP(faultIdentify.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterCalculateUtil.IJMP(faultIdentify.getCPhaseCurrentData());

            double UAJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getAPhaseVoltageData());
            double UBJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getBPhaseVoltageData());
            double UCJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getCPhaseVoltageData());

            double IA10 = FrequencyCharacterCalculateUtil.I10(faultIdentify.getAPhaseCurrentData());
            double IB10 = FrequencyCharacterCalculateUtil.I10(faultIdentify.getBPhaseCurrentData());
            double IC10 = FrequencyCharacterCalculateUtil.I10(faultIdentify.getCPhaseCurrentData());

            double UA10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getAPhaseVoltageData());
            double UB10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getBPhaseVoltageData());
            double UC10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getCPhaseVoltageData());
            if (
                    (UAJMP < 0 && UA10 < AnalysisConstants.UMIN && UBJMP < 0 && UB10 < AnalysisConstants.UMIN && UCJMP < 0 && UC10 < AnalysisConstants.UMIN)
                            ||
                            (IAJMP < 0 && IA10 < AnalysisConstants.IMIN && IBJMP < 0 && IB10 < AnalysisConstants.IMIN && ICJMP < 0 && IC10 < AnalysisConstants.IMIN)
            ) {
                return 1;
            }
        }
        return 0;
    }

}
