package com.hy.biz.dataAnalysis.typeAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 工频量特征计算
 */
public class FrequencyCharacterUtil {


    /**
     * X相故障前后的电流有效值变化量
     * 电流幅值突变量 可带入A相电流、B相电流，分别代表各自计算值
     *
     * @param data
     * @return
     */
    public static double IJMP(Double[] data) {
        double i5 = TypeAlgorithmUtil.calculateCyclicWavePH(data, 5, 256);
        double i6 = TypeAlgorithmUtil.calculateCyclicWavePH(data, 6, 256);
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
    public static double UJMP(Double[] data) {
        double u5 = TypeAlgorithmUtil.calculateCyclicWavePH(data, 5, 256);
        double u6 = TypeAlgorithmUtil.calculateCyclicWavePH(data, 6, 256);
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
    public static double I10(Double[] data) {
        return TypeAlgorithmUtil.calculateCyclicWavePH(data, 10, 256);
    }


    /**
     * X相电压中第10个周波有效值
     *
     * @param data
     * @return
     */
    public static double U10(Double[] data) {
        return TypeAlgorithmUtil.calculateCyclicWavePH(data, 10, 256);
    }

    /**
     * X相电流中第1个周波有效值
     *
     * @param data
     * @return
     */
    public static double I1(Double[] data) {
        return TypeAlgorithmUtil.calculateCyclicWavePH(data, 1, 256);
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
    public static double IMAX10(Double[] data) {
        List<Double> I0List = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            // 计算周波有效值
            I0List.add(TypeAlgorithmUtil.calculateCyclicWavePH(data, i + 1, 256));
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
    public static double IMIN10(Double[] data) {
        List<Double> I0List = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            // 计算周波有效值
            I0List.add(TypeAlgorithmUtil.calculateCyclicWavePH(data, i + 1, 256));
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
        Double[] max = {IAMAX10, IBMAX10, ICMAX10};
        Double[] min = {IAMIN10, IBMIN10, ICMIN10};

        double IMAX = Arrays.stream(max).max(Double::compareTo).get();
        double IMIN = Arrays.stream(min).max(Double::compareTo).get();

        return (IMAX - IMIN) / IMAX;
    }

}
