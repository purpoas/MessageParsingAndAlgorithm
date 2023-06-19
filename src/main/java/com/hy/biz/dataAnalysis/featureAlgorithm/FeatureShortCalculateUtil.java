package com.hy.biz.dataAnalysis.featureAlgorithm;

import com.hy.biz.dataAnalysis.algorithmUtil.AnalysisConstants;
import com.hy.biz.dataAnalysis.algorithmUtil.EjmlUtil;
import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyDTO;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyPoleDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.typeAlgorithm.FrequencyCharacterCalculateUtil;
import com.hy.biz.dataAnalysis.typeAlgorithm.TypeCalculateUtil;
import com.hy.biz.dataParsing.constants.MessageType;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FeatureShortCalculateUtil {

    public static final int CUR_MAX = 800;

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
    public static Double faultCur(FaultIdentifyDTO faultType) {

        // 如果故障相电流数据为故障区间下游数据则不进行故障电流计算
        if (!faultType.isUpstream()) return null;

        List<double[]> list = new ArrayList<>();
        list.add(faultType.getAPhaseCurrentData());
        list.add(faultType.getBPhaseCurrentData());
        list.add(faultType.getCPhaseCurrentData());

        List<Double> faultCurList = new ArrayList<>();
        for (double[] data : list) {
            double IXJMP = TypeCalculateUtil.calculateCyclicWavePH(data, 5, AnalysisConstants.CYCLE_WAVE_LENGTH);

            if (IXJMP >= 0) {

                double[] cur = filterFaultCur(data);

                double TS = 0.000078125;

                double[][] aArray = new double[cur.length][2];
                double[][] bArray = new double[cur.length][1];
                double tn = 0;
                double w = 2 * Math.PI * 50; // ω=2πf，f=50Hz
                for (int i = 0; i < cur.length; i++) {
                    aArray[i][0] = Math.sin(w * tn);
                    aArray[i][1] = Math.cos(w * 0);
                    bArray[i][0] = i;
                    tn = tn + TS;
                }

                // 最小二乘算法
                double[] IfArray = EjmlUtil.calculate(aArray, bArray);
                double If = IfArray[0];
                faultCurList.add(If);
            }

        }

        if (CollectionUtils.isEmpty(faultCurList)) return null;

        return Collections.max(faultCurList);
    }

    public static Integer protectType(FaultIdentifyDTO faultType, Double faultCur, Double faultCurFixI, Double faultCurFixII) {
        if (!faultType.isUpstream() || faultCur == null) return null;

        if (faultCurFixI != null && faultCurFixII != null) {
            if (faultCur > faultCurFixI) {
                return 1;
            } else if (faultCur > faultCurFixII && faultCur < faultCurFixI) {
                return 2;
            } else {
                return null;
            }
        }

        List<double[]> list = new ArrayList<>();
        list.add(faultType.getAPhaseCurrentData());
        list.add(faultType.getBPhaseCurrentData());
        list.add(faultType.getCPhaseCurrentData());

        for (double[] data : list) {
            double IXJMP = TypeCalculateUtil.calculateCyclicWavePH(data, 5, AnalysisConstants.CYCLE_WAVE_LENGTH);
            if (IXJMP >= 0) {
                int length = data.length / AnalysisConstants.CYCLE_WAVE_LENGTH;
                for (int i = 0; i < length; i++) {
                    double IK = TypeCalculateUtil.calculateCyclicWavePH(data, i, AnalysisConstants.CYCLE_WAVE_LENGTH);
                    if (IK < AnalysisConstants.IMIN && Math.abs(i - 5) < 2) {
                        return 1;
                    }
                }
            }
        }

        return 2;
    }

    public static Integer areStat(List<FaultWave> faultWaveList) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssnnnnnnnnn");

        // 找到故障波形时间最小时间
        FaultWave minHeadTimeWave = faultWaveList.stream().min(Comparator.comparing(FaultWave::getHeadTime)).get();

        LocalDateTime minHeadTime = LocalDateTime.parse(minHeadTimeWave.getHeadTime(), dateTimeFormatter).plusNanos(500000000);

        List<FaultWave> f = faultWaveList.stream().filter(faultWave -> faultWave.getWaveType() == MessageType.FAULT_CURRENT).filter(faultWave -> {
            LocalDateTime currentTime = LocalDateTime.parse(faultWave.getHeadTime(), dateTimeFormatter);
            if (currentTime.isAfter(minHeadTime)) return true;
            return false;
        }).collect(Collectors.toList());

        // 找三相电流杆塔
        List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(f);
        List<String> poleIds = threePhaseCurrents.stream().map(FaultIdentifyPoleDTO::getPoleId).distinct().collect(Collectors.toList());
        // 找三相电流三相电压杆塔
        List<FaultIdentifyPoleDTO> threePhaseVoltages = CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(poleIds, f);

        // 是否重合闸成功
        boolean isAre = false;

        for (FaultIdentifyPoleDTO faultIdentify : threePhaseVoltages) {

            double IAJMP = FrequencyCharacterCalculateUtil.IJMP(faultIdentify.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterCalculateUtil.IJMP(faultIdentify.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterCalculateUtil.IJMP(faultIdentify.getCPhaseCurrentData());

            double UAJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getAPhaseVoltageData());
            double UBJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getBPhaseVoltageData());
            double UCJMP = FrequencyCharacterCalculateUtil.UJMP(faultIdentify.getCPhaseVoltageData());

            double IA1 = FrequencyCharacterCalculateUtil.I1(faultIdentify.getAPhaseCurrentData());
            double IB1 = FrequencyCharacterCalculateUtil.I1(faultIdentify.getBPhaseCurrentData());
            double IC1 = FrequencyCharacterCalculateUtil.I1(faultIdentify.getCPhaseCurrentData());

            double UA1 = FrequencyCharacterCalculateUtil.U1(faultIdentify.getAPhaseVoltageData());
            double UB1 = FrequencyCharacterCalculateUtil.U1(faultIdentify.getBPhaseVoltageData());
            double UC1 = FrequencyCharacterCalculateUtil.U1(faultIdentify.getCPhaseVoltageData());

            if ((UAJMP > 0 && UA1 < AnalysisConstants.UMIN)
                    || (UBJMP > 0 && UB1 < AnalysisConstants.UMIN)
                    || (UCJMP > 0 && UC1 < AnalysisConstants.UMIN)
                    || (IAJMP > 0 && IA1 < AnalysisConstants.IMIN)
                    || (IBJMP > 0 && IB1 < AnalysisConstants.IMIN)
                    || (ICJMP > 0 && IC1 < AnalysisConstants.IMIN)
            ) {
                isAre = true;
                break;
            }
        }

        if (!isAre) return 0;

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

            double UA10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getAPhaseCurrentData());
            double UB10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getBPhaseCurrentData());
            double UC10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getCPhaseCurrentData());

            if ((UAJMP > 0 && UA10 < AnalysisConstants.UMIN)
                    || (UBJMP > 0 && UB10 < AnalysisConstants.UMIN)
                    || (UCJMP > 0 && UC10 < AnalysisConstants.UMIN)
                    || (IAJMP > 0 && IA10 < AnalysisConstants.IMIN)
                    || (IBJMP > 0 && IB10 < AnalysisConstants.IMIN)
                    || (ICJMP > 0 && IC10 < AnalysisConstants.IMIN)
            ) {
                isAre = false;
                break;
            }

        }

        if (!isAre) return 0;

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

            double UA10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getAPhaseCurrentData());
            double UB10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getBPhaseCurrentData());
            double UC10 = FrequencyCharacterCalculateUtil.U10(faultIdentify.getCPhaseCurrentData());

            if ((UAJMP < 0 && UA10 < AnalysisConstants.UMIN)
                    || (UBJMP < 0 && UB10 < AnalysisConstants.UMIN)
                    || (UCJMP < 0 && UC10 < AnalysisConstants.UMIN)
                    || (IAJMP < 0 && IA10 < AnalysisConstants.IMIN)
                    || (IBJMP < 0 && IB10 < AnalysisConstants.IMIN)
                    || (ICJMP < 0 && IC10 < AnalysisConstants.IMIN)
            ) {
                isAre = false;
                break;
            }
        }

        if (!isAre) return 0;

        return 1;
    }

    /**
     * 过滤出最小二乘算法计算使用到的故障电流
     * 对电流i第6个周波开始，依次向后查询第一个过零点i_1到第一个大于800的点i_N（假设有N个点，N最大取20）
     *
     * @param data
     * @return
     */
    private static double[] filterFaultCur(double[] data) {
        double[] cur = new double[20];

        double[] i6 = new double[AnalysisConstants.CYCLE_WAVE_LENGTH];

        // 取第六个周波的数据
        System.arraycopy(data, 5 * AnalysisConstants.CYCLE_WAVE_LENGTH, i6, 0, i6.length);

        // 取20个点 过零点I1到In
        Integer zeroIndex = null;
        Integer maxIndex = null;
        for (int i = 0; i < i6.length; i++) {

            if (i6[i] == 0) zeroIndex = i;

            if (zeroIndex != null && i6[i] > CUR_MAX) {
                maxIndex = i;
                break;
            }
        }

        if (maxIndex == null) {
            // 取zeroIndex向后20个点
            System.arraycopy(i6, zeroIndex, cur, 0, cur.length);
        } else {
            // 取maxIndex向前20个点
            System.arraycopy(i6, maxIndex - 20, cur, 0, cur.length);
        }

        return cur;
    }


}
