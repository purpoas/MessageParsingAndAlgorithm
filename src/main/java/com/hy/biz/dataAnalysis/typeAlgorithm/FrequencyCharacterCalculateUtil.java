package com.hy.biz.dataAnalysis.typeAlgorithm;

import com.hy.config.AnalysisConstants;
import com.hy.biz.dataAnalysis.extraAlgorithm.ExtraAlgorithmUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 工频量特征计算
 */
public class FrequencyCharacterCalculateUtil {

    /**
     * 计算周波有效值  3.7.1 计算各周波幅值和相位
     *
     * @param data             波形内容
     * @param cyclicWaveSerial 周波序号
     * @param cyclicWaveLength 周波长度 默认256
     * @return 周波对应有效值
     */
    public static double calculateCyclicWavePH(double[] data, int cyclicWaveSerial, int cyclicWaveLength) {

        // 工频波形长度不满足大于10倍周波长度 不参与判断
        if (data.length < 10 * cyclicWaveLength) return 0.0;

        Double[] in = new Double[cyclicWaveLength];

        int cyclicWaveIndexSum = data.length / cyclicWaveLength;

        // 计算的周波超出波形长度 异常返回
        if (cyclicWaveSerial > cyclicWaveIndexSum) return 0.0;

        // 截取波形内容中对应的周波
        System.arraycopy(data, (cyclicWaveSerial - 1) * cyclicWaveLength, in, 0, in.length);

        int n = 1;

        double xrn = 0.0;
        double xin = 0.0;
        for (int i = 0; i < in.length; i++) {
            xrn += (2 * in[i] * Math.cos(2 * Math.PI * n * i / cyclicWaveLength)) / cyclicWaveLength;
            xin += -(2 * in[i] * Math.cos(2 * Math.PI * n * i / cyclicWaveLength)) / cyclicWaveLength;
        }

        double xn = Math.atan(xin / xrn);
        // φn 暂时计算不使用
//        double φn = Math.sqrt(xin * xin + xrn * xrn);
//        if (xrn == 0) {
//            φn = 1.5 * Math.PI;
//        } else if (xrn > 0) {
//            φn = φn;
//        } else {
//            φn = φn + Math.PI;
//        }

        double ph = xn / Math.sqrt(2);

        return ph;
    }


    /**
     * 零序电压电流计算  3.7.3 零序电压电流计算
     *
     * @param aData A相波形内容
     * @param bData B相波形内容
     * @param cData C相波形内容
     * @return 零序电流值
     */
    public static double calculateZeroCurrent(double[] aData, double[] bData, double[] cData) {

        double[] i0 = synthesisZeroCurrent(aData, bData, cData);

        int cyclicWaveIndexSum = i0.length / AnalysisConstants.CYCLE_WAVE_LENGTH;

        List<Double> I0List = new ArrayList<>();

        for (int i = 0; i < cyclicWaveIndexSum; i++) {
            // 计算周波有效值
            I0List.add(calculateCyclicWavePH(i0, i + 1, AnalysisConstants.CYCLE_WAVE_LENGTH));
        }

        // 取各波周有效值的最大值
        return Collections.max(I0List);
    }

    /**
     * 合成零序电压电流  3.7.3 (1) 零序电压电流计算
     *
     * @param aData A相波形内容
     * @param bData B相波形内容
     * @param cData C相波形内容
     * @return 合成零序电流
     */
    public static double[] synthesisZeroCurrent(double[] aData, double[] bData, double[] cData) {
        int aLength = aData.length;
        int bLength = bData.length;
        int cLength = cData.length;
        int i0Length = 0;

        // 找出三相波形中长度最短的
        if (aLength < bLength && aLength < cLength) {
            i0Length = aLength;
        } else if (bLength < aLength && bLength < cLength) {
            i0Length = bLength;
        } else if (cLength < aLength && cLength < bLength) {
            i0Length = cLength;
        }

        // 波形预处理
        aData = ExtraAlgorithmUtil.preProcessFrequencyWave(aData);
        bData = ExtraAlgorithmUtil.preProcessFrequencyWave(aData);
        cData = ExtraAlgorithmUtil.preProcessFrequencyWave(aData);

        double[] i0 = new double[i0Length];

        // 波形叠加
        for (int i = 0; i < i0Length; i++) {
            i0[i] = aData[i] + bData[i] + cData[i];
        }

        return i0;
    }


    /**
     * X相故障前后的电流有效值变化量
     * 电流幅值突变量 可带入A相电流、B相电流，分别代表各自计算值
     *
     * @param data
     * @return
     */
    public static double IJMP(double[] data) {
        double i5 = calculateCyclicWavePH(data, 5, AnalysisConstants.CYCLE_WAVE_LENGTH);
        double i6 = calculateCyclicWavePH(data, 6, AnalysisConstants.CYCLE_WAVE_LENGTH);
        double diff = i6 - i5;
        if (Math.abs(diff) < 5) return 0;
        return i6 - i5;
    }

    /**
     * X相故障前后的电压有效值变化量
     * 电压幅值突变量 可带入A相电压、B相电压，分别代表各自计算值
     *
     * @param data
     * @return
     */
    public static double UJMP(double[] data) {
        double u5 = calculateCyclicWavePH(data, 5, AnalysisConstants.CYCLE_WAVE_LENGTH);
        double u6 = calculateCyclicWavePH(data, 6, AnalysisConstants.CYCLE_WAVE_LENGTH);
        double diff = u6 - u5;
        if (Math.abs(diff) < 200) return 0;
        return u6 - u5;
    }

    /**
     * X相电流中第10个周波有效值
     *
     * @param data
     * @return
     */
    public static double I10(double[] data) {
        return calculateCyclicWavePH(data, 10, AnalysisConstants.CYCLE_WAVE_LENGTH);
    }


    /**
     * X相电压中第10个周波有效值
     *
     * @param data
     * @return
     */
    public static double U10(double[] data) {
        return calculateCyclicWavePH(data, 10, AnalysisConstants.CYCLE_WAVE_LENGTH);
    }

    /**
     * X相电流中第1个周波有效值
     *
     * @param data
     * @return
     */
    public static double I1(double[] data) {
        return calculateCyclicWavePH(data, 1, AnalysisConstants.CYCLE_WAVE_LENGTH);
    }

    /**
     * X相电压中第1个周波有效值
     *
     * @param data
     * @return
     */
    public static double U1(double[] data) {
        return calculateCyclicWavePH(data, 1, AnalysisConstants.CYCLE_WAVE_LENGTH);
    }


    /**
     * 电流最大值
     * 计算方式 ： 短路保护整定值或者取600A、IA1的2倍、IB1的2倍、IC1的2倍中最大值
     *
     * @param constant
     * @param IA1
     * @param IB1
     * @param IC1
     * @return
     */
    public static double IMAX(double constant, double IA1, double IB1, double IC1) {
        Double[] d = {constant, IA1 * 2, IB1 * 2, IC1 * 2};
        return Arrays.stream(d).max(Double::compareTo).get();
    }

    /**
     * 正常负荷电流
     * 计算方式 ： IA1的1.2倍、IB1的1.2倍、IC1的1.2倍中最大值
     *
     * @param IA1 A相电流中第1个周波有效值
     * @param IB1 B相电流中第1个周波有效值
     * @param IC1 C相电流中第1个周波有效值
     * @return
     */
    public static double INORM(double IA1, double IB1, double IC1) {
        Double[] d = {IA1 * 1.2, IB1 * 1.2, IC1 * 1.2};
        return Arrays.stream(d).max(Double::compareTo).get();
    }

    /**
     * 10周波电流中每周波有效值的最大值
     *
     * @param data
     * @return
     */
    public static double IMAX10(double[] data) {
        List<Double> I0List = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            // 计算周波有效值
            I0List.add(calculateCyclicWavePH(data, i + 1, AnalysisConstants.CYCLE_WAVE_LENGTH));
        }

        // 取各波周有效值的最大值
        return Collections.max(I0List);
    }

    /**
     * 10周波电流中每周波有效值的最大值
     *
     * @param data
     * @return
     */
    public static double IMIN10(double[] data) {
        List<Double> I0List = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            // 计算周波有效值
            I0List.add(calculateCyclicWavePH(data, i + 1, AnalysisConstants.CYCLE_WAVE_LENGTH));
        }

        // 取各波周有效值的最小值
        return Collections.min(I0List);
    }


    /**
     * 三相电流不平衡度
     *
     * @param IAMAX10
     * @param IBMAX10
     * @param ICMAX10
     * @param IAMIN10
     * @param IBMIN10
     * @param ICMIN10
     * @return
     */
    public static double IBPH(double IAMAX10, double IBMAX10, double ICMAX10, double IAMIN10, double IBMIN10, double ICMIN10) {
        double[] max = {IAMAX10, IBMAX10, ICMAX10};
        double[] min = {IAMIN10, IBMIN10, ICMIN10};

        double IMAX = Arrays.stream(max).max().getAsDouble();
        double IMIN = Arrays.stream(min).max().getAsDouble();

        return (IMAX - IMIN) / IMAX;
    }

}
