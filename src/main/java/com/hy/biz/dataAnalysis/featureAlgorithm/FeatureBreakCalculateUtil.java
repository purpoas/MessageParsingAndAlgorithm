package com.hy.biz.dataAnalysis.featureAlgorithm;

import com.hy.biz.dataAnalysis.algorithmUtil.AnalysisConstants;
import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyDTO;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyPoleDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.typeAlgorithm.FrequencyCharacterCalculateUtil;
import com.hy.biz.dataAnalysis.typeAlgorithm.TypeCalculateUtil;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureBreakCalculateUtil {

    /**
     * 故障相位转换
     *
     * @param faultType
     * @return 1-A相断线, 2-B相断线，3-C相断线 默认A相
     */
    public static int faultPhaseId(String faultType) {
        if (AnalysisConstants.FAULT_NATURE_BREAK_A.equals(faultType)) {
            return 1;
        } else if (AnalysisConstants.FAULT_NATURE_BREAK_B.equals(faultType)) {
            return 2;
        } else if (AnalysisConstants.FAULT_NATURE_BREAK_C.equals(faultType)) {
            return 3;
        } else {
            return 1;
        }
    }

    /**
     * 分闸状态分析
     *
     * @param areaLocateDTO
     * @param faultWaveList
     * @return 0-未分闸，1-保护分闸 默认无结果返回0
     */
    public static int isBreak(FaultIdentifyDTO faultType, AreaLocateDTO areaLocateDTO, List<FaultWave> faultWaveList) {

        if (!faultType.isUpstream()) return 0;

        if (!StringUtils.hasText(areaLocateDTO.getFaultHeadTowerId())) return 0;

        List<FaultWave> leftWave = faultWaveList.stream().filter(faultWave -> faultWave.getDistanceToHeadStation() < areaLocateDTO.getFaultHeadTowerDistanceToHeadStation()).collect(Collectors.toList());

        // 找三相电流杆塔
        List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(leftWave);

        List<String> poleIds = threePhaseCurrents.stream().map(FaultIdentifyPoleDTO::getPoleId).distinct().collect(Collectors.toList());
        // 找三相电流三相电压杆塔
        List<FaultIdentifyPoleDTO> threePhaseVoltages = CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(poleIds, leftWave);

        for (FaultIdentifyPoleDTO faultIdentify : threePhaseVoltages) {

            double UAJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getAPhaseVoltageData());
            double UBJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getBPhaseVoltageData());
            double UCJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getCPhaseVoltageData());

            double UA10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getAPhaseVoltageData());
            double UB10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getBPhaseVoltageData());
            double UC10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getCPhaseVoltageData());

            if ((UAJMP < 0 && UA10 < AnalysisConstants.UMIN)
                    || (UBJMP < 0 && UB10 < AnalysisConstants.UMIN)
                    || (UCJMP < 0 && UC10 < AnalysisConstants.UMIN)
            ) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 零序电流特征计算
     *
     * @param faultIdentifyDTO
     * @return
     */
    public static double zeroSeqCur(FaultIdentifyDTO faultIdentifyDTO) {

        double[] i0 = TypeCalculateUtil.synthesisZeroCurrent(faultIdentifyDTO.getAPhaseCurrentData(), faultIdentifyDTO.getBPhaseCurrentData(), faultIdentifyDTO.getCPhaseCurrentData());

        double zero = TypeCalculateUtil.calculateCyclicWavePH(i0, 5, AnalysisConstants.CYCLE_WAVE_LENGTH);

        return zero / 3;
    }

    /**
     * 负序电流特征计算
     *
     * @param aData
     * @param bData
     * @param cData
     * @return
     */
    public static double negSeq(double[] aData, double[] bData, double[] cData) {

        double[] bMove = new double[85];
        double[] cMove = new double[42];
        Arrays.fill(bMove, 0.0);
        Arrays.fill(cMove, 0.0);

        double[] bNewData = new double[bData.length + bMove.length];
        System.arraycopy(bMove, 0, bNewData, 0, bMove.length);
        System.arraycopy(bData, 0, bNewData, bMove.length, bData.length);

        double[] cNewData = new double[cData.length + cMove.length];
        System.arraycopy(cMove, 0, cNewData, 0, cMove.length);
        System.arraycopy(cData, 0, cNewData, cMove.length, cData.length);

        // 找出 aData bNewData cNewData 长度最短的波形
        int aLength = aData.length;
        int bLength = bNewData.length;
        int cLength = cNewData.length;
        int i0Length = 0;

        // 找出三相波形中长度最短的
        if (aLength < bLength && aLength < cLength) {
            i0Length = aLength;
        } else if (bLength < aLength && bLength < cLength) {
            i0Length = bLength;
        } else if (cLength < aLength && cLength < bLength) {
            i0Length = cLength;
        }

        double[] u2 = new double[i0Length - AnalysisConstants.CYCLE_WAVE_LENGTH];
        for (int i = AnalysisConstants.CYCLE_WAVE_LENGTH; i < i0Length; i++) {
            u2[i - AnalysisConstants.CYCLE_WAVE_LENGTH] = (aData[i] + bNewData[i] - cNewData[i]) / 3;
        }

        int cyclicWaveIndexSum = u2.length / AnalysisConstants.CYCLE_WAVE_LENGTH;

        List<Double> I0List = new ArrayList<>();

        for (int i = 0; i < cyclicWaveIndexSum; i++) {
            // 计算周波有效值
            I0List.add(TypeCalculateUtil.calculateCyclicWavePH(u2, i + 1, AnalysisConstants.CYCLE_WAVE_LENGTH));
        }

        // 取各波周有效值的最大值
        return Collections.max(I0List);
    }


}
