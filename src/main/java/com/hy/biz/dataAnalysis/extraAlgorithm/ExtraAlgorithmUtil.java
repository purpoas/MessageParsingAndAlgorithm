package com.hy.biz.dataAnalysis.extraAlgorithm;

import com.hy.biz.dataAnalysis.algorithmUtil.Complex;
import com.hy.biz.dataAnalysis.algorithmUtil.FFT;
import com.hy.config.HyConfigProperty;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 故障额外算法 eg : 波形预处理方法 、 波形有效性检验方法 、 合闸涌流计算方法 、 波形极性计算方法
 */
public class ExtraAlgorithmUtil {

    /**
     * 行波波形预处理
     *
     * @param trvlWaveData 波形数据
     * @return 预处理后的波形数据
     */
    public static Double[] preProcessTravellingWave(String trvlWaveData) {
        Double[] data = preProcessInputData(trvlWaveData);
        if (data.length < 200) return data;

        // Calculate average of first 200 elements
        double avg = Arrays.stream(data, 0, 200).mapToDouble(Double::doubleValue).sum() / 200;

        // Subtract avg from each element
        return Arrays.stream(data).map(d -> d - avg).toArray(Double[]::new);
    }

    /**
     * 工频电流、电压波形预处理
     *
     * @param freqWaveData 波形数据
     * @return 预处理后的波形数据
     */
    public static Double[] preProcessFrequencyWave(Double[] freqWaveData) {
        //校验数据长度
        if (freqWaveData.length < 768) return freqWaveData;

        // Calculate the average of the first 768 points
        double avg = Arrays.stream(freqWaveData, 0, 768).mapToDouble(Double::doubleValue).sum() / 768;

        // Subtract the average from each point in the freqWaveData
        return Arrays.stream(freqWaveData).map(d -> d - avg).toArray(Double[]::new);
    }


    /**
     * 行波电流有效性检验(是否是故障波形)
     *
     * @return 是否为故障波形
     */
    public static boolean isValidTravellingWave(String trvlWaveData) {

        Double[] data = preProcessInputData(trvlWaveData);
        DoubleSummaryStatistics stats = Arrays.stream(data).collect(Collectors.summarizingDouble(Double::doubleValue));

        double maxMinDif = Math.abs(stats.getMax() - stats.getMin());
        double threshold = 5.0;  // 设备默认阈值：5A

        return maxMinDif >= threshold;
    }


    /**
     * 分析该波形的工频电流是否有效(是否是故障波形)
     *
     * @param inputData 数据
     * @return 工频电流是否有效
     */
    public static boolean isValidPowerFreqCurrentOrVoltage(String inputData, HyConfigProperty hyConfigProperty) {
        Double[] data = preProcessInputData(inputData);
        return checkFundamentalFrequency(data, hyConfigProperty) && checkPhaseCurrentAbruptChange(data, hyConfigProperty);
    }

    /**
     * 判断行波波形极性
     *
     * @return true +  false -
     */
    public boolean judgeTravelWaveAbsolute(Double[] data) {
        return false;
    }

    // 私有方法==========================================================================================================

    /**
     * 基于基波频率的判断方式检验工频电流/工频电压的有效性
     *
     * @return 是否为故障波形
     */
    private static boolean checkFundamentalFrequency(Double[] signalData, HyConfigProperty hyConfigProperty) {
        double maxSignalThreshold = hyConfigProperty.getConstant().getFrequencyMaxThreshold();
        long frequencySamplingRate = hyConfigProperty.getConstant().getDeviceSampleRate();

        double maxSignalValue = Arrays.stream(signalData).mapToDouble(Math::abs).max().orElse(0.0);

        if (maxSignalValue > maxSignalThreshold) return false;

        // 低通滤波器
        Double[] lowPassFilter = {0.00149176912699628, 0.00113338606406803, 0.000286402296623436, -0.00169769035032670,
                -0.00517520815333871, -0.00964292315834651, -0.0134164361793712, -0.0137898593712167, -0.00773459964570550,
                0.00703454548213997, 0.0309850620686805, 0.0621043298912804, 0.0959667403435662, 0.126609163946671,
                0.148003498647870, 0.155683637980818, 0.148003498647870, 0.126609163946671, 0.0959667403435662, 0.0621043298912804,
                0.0309850620686805, 0.00703454548213997, -0.00773459964570550, -0.0137898593712167, -0.0134164361793712,
                -0.00964292315834651, -0.00517520815333871, -0.00169769035032670, 0.000286402296623436, 0.00113338606406803,
                0.00149176912699628};

        Double[] filteredSignal = FFT.linearConvolve(signalData, lowPassFilter);
        Complex[] complexSignalForFFT = new Complex[4096];
        int complexSignalLength = Math.min(filteredSignal.length - 40, 4096);

        IntStream.range(0, complexSignalLength).forEach(i -> complexSignalForFFT[i] = new Complex(filteredSignal[i + 20], 0));
        IntStream.range(complexSignalLength, 4096).forEach(i -> complexSignalForFFT[i] = new Complex(0, 0));

        Complex[] fftResult = FFT.fft(complexSignalForFFT);
        double normalizationFactor = 2.0 / (filteredSignal.length - 40);

        int peakFrequencyIndex = IntStream.range(1, fftResult.length / 2 + 1)
                .reduce(0, (currentMaxIndex, i)
                        -> fftResult[i].abs() * normalizationFactor > fftResult[currentMaxIndex].abs() * normalizationFactor ? i : currentMaxIndex);

        double peakFrequencyHz = (double) frequencySamplingRate / 4096 * peakFrequencyIndex;

        return peakFrequencyHz < 52 && 48 < peakFrequencyHz;
    }


    /**
     * 基于相电流突变量的判断方式检验工频电流/工频电压的有效性(是否是故障波形)
     *
     * @return 是否为故障波形
     */
    private static boolean checkPhaseCurrentAbruptChange(Double[] data, HyConfigProperty hyConfigProperty) {
        double sampleRate = hyConfigProperty.getConstant().getDeviceSampleRate();   // 设备采样率 12800
        double threshold = hyConfigProperty.getConstant().getFaultFrequencyCurrentThreshold(); // 工频电流故障触发阈值 5

        int N = (int) (sampleRate / 50); // N个点代表一个周波

        if (data.length < 2 * N + 1)
            throw new IllegalArgumentException("数据长度不足");

        double[] abruptAmounts = new double[data.length - 2 * N];
        for (int n = 2 * N; n < data.length; n++) {
            double abruptAmount = Math.abs(Math.abs(data[n] - data[n - N]) - Math.abs(data[n - N] - data[n - 2 * N]));
            abruptAmounts[n - 2 * N] = abruptAmount;
        }

        // 忽略前两个周波
        int ignorePoints = 2 * N;
        double[] dataAfterIgnore = Arrays.copyOfRange(abruptAmounts, ignorePoints, abruptAmounts.length);

        // 求取 MAX
        double MAX = Arrays.stream(dataAfterIgnore).max().orElse(Double.NEGATIVE_INFINITY);

        // 	输出有效性：MAX > threshold 且突变量曲线中存在连续三个点均 > Th_power即为波形有效，否则无效。
        if (MAX > threshold) {
            for (int i = 0; i < dataAfterIgnore.length - 2; i++) {
                if (dataAfterIgnore[i] > threshold && dataAfterIgnore[i + 1] > threshold && dataAfterIgnore[i + 2] > threshold) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Double[] preProcessInputData(String inputData) {
        return Arrays.stream(inputData.split(","))
                .map(Double::parseDouble)
                .toArray(Double[]::new);
    }


}
