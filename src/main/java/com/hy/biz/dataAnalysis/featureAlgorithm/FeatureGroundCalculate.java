package com.hy.biz.dataAnalysis.featureAlgorithm;

import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.*;
import com.hy.biz.dataAnalysis.typeAlgorithm.FaultIdentifyAlgorithmUtil;
import com.hy.biz.dataAnalysis.typeAlgorithm.FrequencyCharacterUtil;
import com.hy.biz.dataAnalysis.typeAlgorithm.TypeAlgorithmUtil;
import com.hy.biz.dataAnalysis.algorithmUtil.AnalysisConstants;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class FeatureGroundCalculate {

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
     * 零序电流特征计算
     *
     * @param faultIdentifyDTO
     * @return
     */
    public static double zeroSeqCur(FaultIdentifyDTO faultIdentifyDTO) {

        double[] i0 = TypeAlgorithmUtil.synthesisZeroCurrent(faultIdentifyDTO.getAPhaseCurrentData(), faultIdentifyDTO.getBPhaseCurrentData(), faultIdentifyDTO.getCPhaseCurrentData());

        double zero = TypeAlgorithmUtil.calculateCyclicWavePH(i0, 5, 256);

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
            double IAJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getCPhaseCurrentData());

            double UAJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getAPhaseVoltageData());
            double UBJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getBPhaseVoltageData());
            double UCJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getCPhaseVoltageData());

            double IA10 = FrequencyCharacterUtil.I10(faultIdentify.getAPhaseCurrentData());
            double IB10 = FrequencyCharacterUtil.I10(faultIdentify.getBPhaseCurrentData());
            double IC10 = FrequencyCharacterUtil.I10(faultIdentify.getCPhaseCurrentData());

            double UA10 = FrequencyCharacterUtil.U10(faultIdentify.getAPhaseVoltageData());
            double UB10 = FrequencyCharacterUtil.U10(faultIdentify.getBPhaseVoltageData());
            double UC10 = FrequencyCharacterUtil.U10(faultIdentify.getCPhaseVoltageData());
            if (
                    (UAJMP < 0 && UA10 < FaultIdentifyAlgorithmUtil.UMIN && UBJMP < 0 && UB10 < FaultIdentifyAlgorithmUtil.UMIN && UCJMP < 0 && UC10 < FaultIdentifyAlgorithmUtil.UMIN)
                            ||
                            (IAJMP < 0 && IA10 < FaultIdentifyAlgorithmUtil.IMIN && IBJMP < 0 && IB10 < FaultIdentifyAlgorithmUtil.IMIN && ICJMP < 0 && IC10 < FaultIdentifyAlgorithmUtil.IMIN)
            ) {
                return 1;
            }
        }
        return 0;
    }

}
