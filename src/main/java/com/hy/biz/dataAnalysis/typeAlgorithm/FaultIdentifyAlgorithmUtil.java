package com.hy.biz.dataAnalysis.typeAlgorithm;

import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyDTO;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyPoleDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataResolver.constants.MessageType;
import com.hy.biz.util.ListUtil;
import com.hy.config.AnalysisConstants;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 故障类型识别：
 * 合闸涌流 、
 * AB两相短路 、AC两相短路 、BC两相短路 、三相短路 、
 * AB两相断路 、AC两相断路 、BC两相断路 、A相断路 、B相断路 、C相断路 、
 * 正常运行状态 、增负荷 、
 * A相接地 、B相接地 、C相接地
 */
public class FaultIdentifyAlgorithmUtil {

    public static final int SHORT_CONSTANT = 600; //短路保护整定值
    public static final int I0AM = 2;   //零序电流阈值
    public static final double IBPH_MAX = 0.9;  //三相不平衡度
    public static final int IMIN = 2;   //最小工作电流
    public static final int UMIN = 600; //最小工电压
    public static final double IH2 = 0.15;  //二次谐波含量


    public static FaultIdentifyDTO judge(Set<FaultWave> faultWaves, AreaLocateDTO areaLocateDTO) {

        // TODO 判断故障左区间是否是小号测 是则进行下游故障类型判断 否则进行上游设备故障类型判断

        // 左区间是小号测
        if (!StringUtils.hasText(areaLocateDTO.getFaultHeadTowerId())) {
            // TODO 下游分析
            List<FaultWave> rightWave = faultWaves.stream().filter(faultWave -> faultWave.getDistanceToHeadStation() > areaLocateDTO.getFaultEndTowerDistanceToHeadStation()).collect(Collectors.toList());

            // 找三相电流杆塔
            List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(rightWave);
            // 计算三相电流波形的故障性质
            FaultIdentifyDTO currentFaultType = calculateDownstreamCurrentFaultType(threePhaseCurrents);

            if (currentFaultType != null) return currentFaultType;

            List<String> poleIds = threePhaseCurrents.stream().map(FaultIdentifyPoleDTO::getPoleId).distinct().collect(Collectors.toList());
            // 找三相电流三相电压杆塔
            List<FaultIdentifyPoleDTO> threePhaseVoltages = CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(poleIds, rightWave);
            // 计算三相电流三相电压波形的故障性质
            FaultIdentifyDTO voltageFaultType = calculateDownStreamVoltageFaultType(threePhaseVoltages);

            if (voltageFaultType != null) return voltageFaultType;

        } else {
            // TODO 上游分析
            List<FaultWave> leftWave = faultWaves.stream().filter(faultWave -> faultWave.getDistanceToHeadStation() < areaLocateDTO.getFaultHeadTowerDistanceToHeadStation()).collect(Collectors.toList());

            // 找三相电流杆塔
            List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(leftWave);
            // 计算三相电流波形的故障性质
            FaultIdentifyDTO currentFaultType = calculateUpstreamCurrentFaultType(threePhaseCurrents);

            if (currentFaultType != null) return currentFaultType;

            List<String> poleIds = threePhaseCurrents.stream().map(FaultIdentifyPoleDTO::getPoleId).distinct().collect(Collectors.toList());
            // 找三相电流三相电压杆塔
            List<FaultIdentifyPoleDTO> threePhaseVoltages = CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(poleIds, leftWave);
            // 计算三相电流三相电压波形的故障性质
            FaultIdentifyDTO voltageFaultType = calculateUpStreamVoltageFaultType(threePhaseVoltages);

            if (voltageFaultType != null) return voltageFaultType;

            // 右区间是大号测
            if (StringUtils.hasText(areaLocateDTO.getFaultEndTowerId())) {
                // TODO 下游分析
                List<FaultWave> rightWave = faultWaves.stream().filter(faultWave -> faultWave.getDistanceToHeadStation() > areaLocateDTO.getFaultEndTowerDistanceToHeadStation()).collect(Collectors.toList());

                // 找三相电流杆塔
                List<FaultIdentifyPoleDTO> downThreePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(rightWave);
                // 计算三相电流波形的故障性质
                FaultIdentifyDTO downCurrentFaultType = calculateDownstreamCurrentFaultType(downThreePhaseCurrents);

                if (downCurrentFaultType != null) return downCurrentFaultType;

                List<String> downPoleIds = threePhaseCurrents.stream().map(FaultIdentifyPoleDTO::getPoleId).distinct().collect(Collectors.toList());
                // 找三相电流三相电压杆塔
                List<FaultIdentifyPoleDTO> downThreePhaseVoltages = CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(downPoleIds, rightWave);
                // 计算三相电流三相电压波形的故障性质
                FaultIdentifyDTO downVoltageFaultType = calculateDownStreamVoltageFaultType(downThreePhaseVoltages);

                if (downVoltageFaultType != null) return downVoltageFaultType;
            }
        }

        return null;
    }

    /**
     * 具备三相电流的杆塔进行故障性质判断 - TODO 上游判断
     *
     * @param faultIdentifyPoles
     * @return
     */
    private static FaultIdentifyDTO calculateUpstreamCurrentFaultType(List<FaultIdentifyPoleDTO> faultIdentifyPoles) {

        for (FaultIdentifyPoleDTO faultIdentify : faultIdentifyPoles) {

            double IAJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getCPhaseCurrentData());

            double IA10 = FrequencyCharacterUtil.I10(faultIdentify.getAPhaseCurrentData());
            double IB10 = FrequencyCharacterUtil.I10(faultIdentify.getBPhaseCurrentData());
            double IC10 = FrequencyCharacterUtil.I10(faultIdentify.getCPhaseCurrentData());

            if (
                    IAJMP > 0 && IBJMP > 0 && ICJMP <= 0 && IA10 < IMIN && IB10 < IMIN && IC10 < IMIN
            ) {
                return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_SHORT_AB, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData());
            } else if (
                    IAJMP > 0 && IBJMP <= 0 && ICJMP > 0 && IA10 < IMIN && IB10 < IMIN && IC10 < IMIN
            ) {
                return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_SHORT_AC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData());
            } else if (
                    IAJMP <= 0 && IBJMP > 0 && ICJMP > 0 && IA10 < IMIN && IB10 < IMIN && IC10 < IMIN
            ) {
                return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_SHORT_BC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData());
            }

            if (!(IAJMP <= 0 && IBJMP > 0 && ICJMP > 0)) {
                // 结束本杆塔分析 切换下一组
                continue;
            }

            // 计算零序电流I0
            double I0 = TypeAlgorithmUtil.calculateZeroCurrent(faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData());

            double IA1 = FrequencyCharacterUtil.I1(faultIdentify.getAPhaseCurrentData());
            double IB1 = FrequencyCharacterUtil.I1(faultIdentify.getBPhaseCurrentData());
            double IC1 = FrequencyCharacterUtil.I1(faultIdentify.getCPhaseCurrentData());

            if (I0 <= I0AM && IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN && IA10 < IMIN && IB10 < IMIN && IC10 < IMIN) {
                // 三相短路
                return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_SHORT_ABC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData());
            } else if (I0 <= I0AM && IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN) {
                // 负荷波动
                return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_LOAD_UNDULATE, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData());
            }
        }

        return null;

    }

    /**
     * 具备三相电压的杆塔进行故障性质判断 - TODO 上游判断
     *
     * @param faultIdentifyPoles
     * @return
     */
    private static FaultIdentifyDTO calculateUpStreamVoltageFaultType(List<FaultIdentifyPoleDTO> faultIdentifyPoles) {
        for (FaultIdentifyPoleDTO faultIdentify : faultIdentifyPoles) {

            double IAJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getCPhaseCurrentData());

            if (IAJMP <= 0 && IBJMP <= 0 && ICJMP <= 0) {

                double I0 = TypeAlgorithmUtil.calculateZeroCurrent(faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData());

                double IA1 = FrequencyCharacterUtil.I1(faultIdentify.getAPhaseCurrentData());
                double IB1 = FrequencyCharacterUtil.I1(faultIdentify.getBPhaseCurrentData());
                double IC1 = FrequencyCharacterUtil.I1(faultIdentify.getCPhaseCurrentData());

                double IA10 = FrequencyCharacterUtil.I10(faultIdentify.getAPhaseCurrentData());
                double IB10 = FrequencyCharacterUtil.I10(faultIdentify.getBPhaseCurrentData());
                double IC10 = FrequencyCharacterUtil.I10(faultIdentify.getCPhaseCurrentData());

                double UAJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getAPhaseVoltageData());
                double UBJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getBPhaseVoltageData());
                double UCJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getCPhaseVoltageData());

                if (I0 <= I0AM && IAJMP == 0 && IBJMP == 0 && ICJMP == 0 && UAJMP == 0 && UBJMP == 0 && UCJMP == 0 && IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_NORMAL, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                } else {

                    double IAM = FrequencyCharacterUtil.IMAX10(faultIdentify.getAPhaseCurrentData());
                    double IBM = FrequencyCharacterUtil.IMAX10(faultIdentify.getBPhaseCurrentData());
                    double ICM = FrequencyCharacterUtil.IMAX10(faultIdentify.getCPhaseCurrentData());

                    double IAMIN10 = FrequencyCharacterUtil.IMIN10(faultIdentify.getAPhaseCurrentData());
                    double IBMIN10 = FrequencyCharacterUtil.IMIN10(faultIdentify.getBPhaseCurrentData());
                    double ICMIN10 = FrequencyCharacterUtil.IMIN10(faultIdentify.getCPhaseCurrentData());

                    double IBPH = FrequencyCharacterUtil.IBPH(IAM, IBM, ICM, IAMIN10, IBMIN10, ICMIN10);

                    double IANORM = IA1 * 1.2;
                    double IBNORM = IB1 * 1.2;
                    double ICNORM = IC1 * 1.2;

                    if (IAM > IMIN && IBM > IMIN && ICM > IMIN && IA1 > IMIN && IB1 > IMIN && IC1 > IMIN && IA10 > IMIN && IB10 > IMIN && IC10 > IMIN
                            && IBPH <= IBPH_MAX && UAJMP < 0 && UBJMP > 0 && UCJMP > 0
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_GROUND_A, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    } else if (IAM > IMIN && IBM > IMIN && ICM > IMIN && IA1 > IMIN && IB1 > IMIN && IC1 > IMIN && IA10 > IMIN && IB10 > IMIN && IC10 > IMIN
                            && IBPH <= IBPH_MAX && UAJMP > 0 && UBJMP < 0 && UCJMP > 0
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_GROUND_B, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    } else if (IAM > IMIN && IBM > IMIN && ICM > IMIN && IA1 > IMIN && IB1 > IMIN && IC1 > IMIN && IA10 > IMIN && IB10 > IMIN && IC10 > IMIN
                            && IBPH <= IBPH_MAX && UAJMP > 0 && UBJMP > 0 && UCJMP < 0
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_GROUND_C, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    } else if (IAM <= IANORM && IBM <= IBNORM && ICM <= ICNORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN
                            && IA10 < IMIN && IB10 < IMIN
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_AB, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    } else if (IAM <= IANORM && IBM <= IBNORM && ICM <= ICNORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN
                            && IA10 < IMIN && IC10 < IMIN
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_AC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    } else if (IAM <= IANORM && IBM <= IBNORM && ICM <= ICNORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN
                            && IB10 < IMIN && IC10 < IMIN
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_BC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    } else if (IAM <= IANORM && IBM <= IBNORM && ICM <= ICNORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN
                            && IA10 < IMIN
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_A, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    } else if (IAM <= IANORM && IBM <= IBNORM && ICM <= ICNORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN
                            && IB10 < IMIN
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_B, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    } else if (IAM <= IANORM && IBM <= IBNORM && ICM <= ICNORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN
                            && IC10 < IMIN
                    ) {
                        return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_C, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData());
                    }
                }
            }
        }
        return null;
    }


    /**
     * 具备三相电流的杆塔进行故障性质判断 - TODO 下游判断
     *
     * @param faultIdentifyPoles
     * @return
     */
    private static FaultIdentifyDTO calculateDownstreamCurrentFaultType(List<FaultIdentifyPoleDTO> faultIdentifyPoles) {

        for (FaultIdentifyPoleDTO faultIdentify : faultIdentifyPoles) {

            double IAJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getCPhaseCurrentData());

            double IA10 = FrequencyCharacterUtil.I10(faultIdentify.getAPhaseCurrentData());
            double IB10 = FrequencyCharacterUtil.I10(faultIdentify.getBPhaseCurrentData());
            double IC10 = FrequencyCharacterUtil.I10(faultIdentify.getCPhaseCurrentData());

            if (
                    IAJMP < 0 && IBJMP < 0 && ICJMP >= 0 && IA10 < IMIN && IB10 < IMIN && IC10 < IMIN
            ) {
                return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_SHORT_AB, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), false);
            } else if (
                    IAJMP < 0 && IBJMP >= 0 && ICJMP < 0 && IA10 < IMIN && IB10 < IMIN && IC10 < IMIN
            ) {
                return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_SHORT_AC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), false);
            } else if (
                    IAJMP >= 0 && IBJMP < 0 && ICJMP < 0 && IA10 < IMIN && IB10 < IMIN && IC10 < IMIN
            ) {
                return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_SHORT_BC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), false);
            }
        }

        return null;

    }

    /**
     * 具备三相电压的杆塔进行故障性质判断 - TODO 下游判断
     *
     * @param faultIdentifyPoles
     * @return
     */
    private static FaultIdentifyDTO calculateDownStreamVoltageFaultType(List<FaultIdentifyPoleDTO> faultIdentifyPoles) {
        for (FaultIdentifyPoleDTO faultIdentify : faultIdentifyPoles) {

            double IAJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterUtil.IJMP(faultIdentify.getCPhaseCurrentData());


            double I0 = TypeAlgorithmUtil.calculateZeroCurrent(faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData());

            double UAJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getAPhaseVoltageData());
            double UBJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getBPhaseVoltageData());
            double UCJMP = FrequencyCharacterUtil.UJMP(faultIdentify.getCPhaseVoltageData());

            double IA1 = FrequencyCharacterUtil.I1(faultIdentify.getAPhaseCurrentData());
            double IB1 = FrequencyCharacterUtil.I1(faultIdentify.getBPhaseCurrentData());
            double IC1 = FrequencyCharacterUtil.I1(faultIdentify.getCPhaseCurrentData());

            double IA10 = FrequencyCharacterUtil.I10(faultIdentify.getAPhaseCurrentData());
            double IB10 = FrequencyCharacterUtil.I10(faultIdentify.getBPhaseCurrentData());
            double IC10 = FrequencyCharacterUtil.I10(faultIdentify.getCPhaseCurrentData());

            double IAM = FrequencyCharacterUtil.IMAX10(faultIdentify.getAPhaseCurrentData());
            double IBM = FrequencyCharacterUtil.IMAX10(faultIdentify.getBPhaseCurrentData());
            double ICM = FrequencyCharacterUtil.IMAX10(faultIdentify.getCPhaseCurrentData());

            double INORM = FrequencyCharacterUtil.INORM(IA1, IB1, IC1);

            if (IAJMP < 0 && IBJMP < 0 && ICJMP < 0) {

                if (I0 <= I0AM && IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN && IA10 < IMIN && IB10 < IMIN && IC10 < IMIN &&
                        UAJMP < 0 && UBJMP < 0 && UCJMP < 0
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_SHORT_ABC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM <= INORM && IBM <= INORM && ICM <= INORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN &&
                        IA10 < IMIN && IB10 < IMIN
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_AB, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM <= INORM && IBM <= INORM && ICM <= INORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN &&
                        IA10 < IMIN && IC10 < IMIN
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_AC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM <= INORM && IBM <= INORM && ICM <= INORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN &&
                        IB10 < IMIN && IC10 < IMIN
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_BC, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM <= INORM && IBM <= INORM && ICM <= INORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN &&
                        IA10 < IMIN
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_A, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM <= INORM && IBM <= INORM && ICM <= INORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN &&
                        IB10 < IMIN
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_B, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM <= INORM && IBM <= INORM && ICM <= INORM && IAM >= IMIN && IBM >= IMIN && ICM >= IMIN &&
                        IC10 < IMIN
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_BREAK_C, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                }

            } else if (IAJMP >= 0 && IBJMP >= 0 && ICJMP >= 0) {

                double IAMAX10 = FrequencyCharacterUtil.IMAX10(faultIdentify.getAPhaseCurrentData());
                double IBMAX10 = FrequencyCharacterUtil.IMAX10(faultIdentify.getBPhaseCurrentData());
                double ICMAX10 = FrequencyCharacterUtil.IMAX10(faultIdentify.getCPhaseCurrentData());

                double IAMIN10 = FrequencyCharacterUtil.IMIN10(faultIdentify.getAPhaseCurrentData());
                double IBMIN10 = FrequencyCharacterUtil.IMIN10(faultIdentify.getBPhaseCurrentData());
                double ICMIN10 = FrequencyCharacterUtil.IMIN10(faultIdentify.getCPhaseCurrentData());
                double IBPH = FrequencyCharacterUtil.IBPH(IAMAX10, IBMAX10, ICMAX10, IAMIN10, IBMIN10, ICMIN10);

                if (I0 < I0AM && IAJMP == 0 && IBJMP == 0 && ICJMP == 0 && UAJMP == 0 && UBJMP == 0 && UCJMP == 0 &&
                        IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_NORMAL, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (I0 < I0AM && IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_INS, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM >= IMIN && IBM >= IMIN && ICM >= IMIN && IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN && IA10 > IMIN && IB10 >= IMIN && IC10 >= IMIN &&
                        IBPH <= IBPH_MAX && UAJMP < 0 && UBJMP >= 0 && UCJMP >= 0
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_GROUND_A, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM >= IMIN && IBM >= IMIN && ICM >= IMIN && IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN && IA10 > IMIN && IB10 >= IMIN && IC10 >= IMIN &&
                        IBPH <= IBPH_MAX && UAJMP >= 0 && UBJMP < 0 && UCJMP >= 0
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_GROUND_B, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                } else if (IAM >= IMIN && IBM >= IMIN && ICM >= IMIN && IA1 >= IMIN && IB1 >= IMIN && IC1 >= IMIN && IA10 > IMIN && IB10 >= IMIN && IC10 >= IMIN &&
                        IBPH <= IBPH_MAX && UAJMP >= 0 && UBJMP >= 0 && UCJMP < 0
                ) {
                    return new FaultIdentifyDTO(AnalysisConstants.FAULT_NATURE_GROUND_C, faultIdentify.getAPhaseCurrentData(), faultIdentify.getBPhaseCurrentData(), faultIdentify.getCPhaseCurrentData(), faultIdentify.getAPhaseVoltageData(), faultIdentify.getBPhaseVoltageData(), faultIdentify.getCPhaseVoltageData(), false);
                }
            }
        }
        return null;
    }


}
