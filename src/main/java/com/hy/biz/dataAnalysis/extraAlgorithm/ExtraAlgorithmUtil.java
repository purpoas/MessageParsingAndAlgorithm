package com.hy.biz.dataAnalysis.extraAlgorithm;

/**
 * 故障额外算法 eg : 波形预处理方法 、 波形有效性检验方法 、 合闸涌流计算方法 、 波形极性计算方法
 */
public class ExtraAlgorithmUtil {


    /**
     * 行波波形预处理
     *
     * @param data
     * @return
     */
    public static Double[] preProccessTravelWave(Double[] data) {

        return null;
    }

    /**
     * 工频电流、电压波形预处理
     *
     * @param data
     * @return
     */
    public static Double[] preProccessFrequencyWave(Double[] data) {
        return null;
    }

    /**
     * 行波波形有效性检验(是否是故障波形)
     *
     * @return
     */
    public static boolean checkTravelWaveEffect(Double[] data) {
        return false;
    }


    /**
     * 工频电流、工频电压波形有效性检验(是否是故障波形)
     *
     * @return
     */
    public boolean checkFrequencyWaveEffect(Double[] data) {
        return false;
    }


    /**
     * 判断行波波形极性
     *
     * @return true +  false -
     */
    public boolean judgeTravelWaveAbsolute(Double[] data) {
        return false;
    }


}
