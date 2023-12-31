package com.hy.biz.dataAnalysis.extraAlgorithm;

import com.hy.biz.dataAnalysis.algorithmUtil.Complex;
import com.hy.biz.dataAnalysis.algorithmUtil.FFT;
import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyPoleDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.dto.FeatureFlowDTO;
import com.hy.biz.dataAnalysis.dto.FeatureUndulateDTO;
import com.hy.biz.dataAnalysis.typeAlgorithm.FrequencyCharacterCalculateUtil;
import com.hy.config.HyConfigProperty;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hy.biz.dataAnalysis.algorithmUtil.FFT.fft;

/**
 * 故障额外算法 eg : 波形预处理方法 、 波形有效性检验方法 、 合闸涌流计算方法 、 波形极性计算方法
 */
public class ExtraAlgorithmUtil {

    /**
     * 行波波形预处理
     *
     * @param data 波形数据
     * @return 预处理后的波形数据
     */
    public static double[] preProcessTravellingWave(double[] data) {
        if (data.length < 200) return data;

        // Calculate average of first 200 elements
        double avg = Arrays.stream(data, 0, 200).sum() / 200;

        // Subtract avg from each element
        return Arrays.stream(data).map(d -> d - avg).toArray();
    }

    /**
     * 工频电流、电压波形预处理
     *
     * @param freqWaveData 波形数据
     * @return 预处理后的波形数据
     */
    public static double[] preProcessFrequencyWave(double[] freqWaveData) {
        //校验数据长度
        if (freqWaveData.length < 768) return freqWaveData;

        // Calculate the average of the first 768 points
        double avg = Arrays.stream(freqWaveData, 0, 768).sum() / 768;

        // Subtract the average from each point in the freqWaveData
        return Arrays.stream(freqWaveData).map(d -> d - avg).toArray();
    }


    /**
     * 行波电流有效性检验(是否是故障波形)
     *
     * @param data            波形内容数组
     * @param travelThreshold 设备默认阈值：5A
     * @return 是否为故障波形
     */
    public static boolean isValidTravellingWave(double[] data, double travelThreshold) {

        double maxValue = data[0];
        double minValue = data[0];
        for (double d : data) {
            if (maxValue > d) {
                maxValue = d;
            }
            if (minValue < d) {
                minValue = d;
            }
        }

        double maxMinDif = Math.abs(maxValue - minValue);

        return maxMinDif >= travelThreshold;
    }


    /**
     * 分析该波形的工频电流是否有效(是否是故障波形)
     *
     * @param data 数据
     * @return 工频电流是否有效
     */
    public static boolean isValidPowerFreqCurrentOrVoltage(double[] data, HyConfigProperty hyConfigProperty) {
        return checkFundamentalFrequency(data, hyConfigProperty);
    }

    /**
     * 合闸涌流判断
     *
     * @param freqWaveDataA    Frequency waveform data for Phase A
     * @param freqWaveDataB    Frequency waveform data for Phase B
     * @param freqWaveDataC    Frequency waveform data for Phase C
     * @param hyConfigProperty Configuration properties for the system
     * @return Pair<Boolean, Double> A pair indicating if a surge is detected (true/false) and the maximum second harmonic content across the phases
     */
    public static Pair<Boolean, Double> isSurge(Double[] freqWaveDataA, Double[] freqWaveDataB, Double[] freqWaveDataC, HyConfigProperty hyConfigProperty) {
        long deviceSampleRate = hyConfigProperty.getConstant().getDeviceSampleRate();

        // Compute FFT and extract 50Hz and 100Hz component for each phase
        Double[] fftPhaseA = fftGetSpecificFrequencies(freqWaveDataA, deviceSampleRate);
        Double[] fftPhaseB = fftGetSpecificFrequencies(freqWaveDataB, deviceSampleRate);
        Double[] fftPhaseC = fftGetSpecificFrequencies(freqWaveDataC, deviceSampleRate);

        // Calculate second harmonic content for each phase by dividing 100Hz component by 50Hz component
        double IA2X = fftPhaseA[1] / fftPhaseA[0];
        double IB2X = fftPhaseB[1] / fftPhaseB[0];
        double IC2X = fftPhaseC[1] / fftPhaseC[0];

        // Find the maximum second harmonic content across the phases
        double maxSecondHarmonic = Math.max(IA2X, Math.max(IB2X, IC2X));

        // Determine if a surge is present. A surge is detected if the second harmonic content is greater than 15% of the fundamental frequency for any phase.
        boolean isSurge = IA2X > 0.15 || IB2X > 0.15 || IC2X > 0.15;

        // Return a pair with the surge detection result and the maximum second harmonic content
        return new Pair<>(isSurge, maxSecondHarmonic);
    }

    /**
     * 合闸涌流判断
     *
     * @param hyConfigProperty Configuration properties for the system
     * @return Pair<Boolean, Double> A pair indicating if a surge is detected (true/false) and the maximum second harmonic content across the phases
     */
    public static FeatureFlowDTO isSurge2(Set<FaultWave> faultWaves, HyConfigProperty hyConfigProperty) {
        FeatureFlowDTO result = null;

        List<FaultWave> faultWaveList = new ArrayList<>(faultWaves);

        List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(faultWaveList);

        for (FaultIdentifyPoleDTO pole : threePhaseCurrents) {

            double[] aData = pole.getAPhaseCurrentData();
            double[] bData = pole.getBPhaseCurrentData();
            double[] cData = pole.getCPhaseCurrentData();

            Complex[] aComplex = CommonAlgorithmUtil.fft4096(aData);
            Complex[] bComplex = CommonAlgorithmUtil.fft4096(bData);
            Complex[] cComplex = CommonAlgorithmUtil.fft4096(cData);

            Double aH1hz = CommonAlgorithmUtil.getMaxHzNew(aComplex, 4096, hyConfigProperty.getConstant().getDeviceSampleRate().intValue(), 45, 55);
            Double aH2hz = CommonAlgorithmUtil.getMaxHzNew(aComplex, 4096, hyConfigProperty.getConstant().getDeviceSampleRate().intValue(), 95, 105);

            Double bH1hz = CommonAlgorithmUtil.getMaxHzNew(bComplex, 4096, hyConfigProperty.getConstant().getDeviceSampleRate().intValue(), 45, 55);
            Double bH2hz = CommonAlgorithmUtil.getMaxHzNew(bComplex, 4096, hyConfigProperty.getConstant().getDeviceSampleRate().intValue(), 95, 105);

            Double cH1hz = CommonAlgorithmUtil.getMaxHzNew(cComplex, 4096, hyConfigProperty.getConstant().getDeviceSampleRate().intValue(), 45, 55);
            Double cH2hz = CommonAlgorithmUtil.getMaxHzNew(cComplex, 4096, hyConfigProperty.getConstant().getDeviceSampleRate().intValue(), 95, 105);

            if (aH2hz > 0.15 * aH1hz || bH2hz > 0.15 * bH1hz || cH2hz > 0.15 * cH1hz) {
                double IA2X = aH1hz / aH2hz;
                double IB2X = bH1hz / bH2hz;
                double IC2X = cH1hz / cH2hz;

                if (IA2X > IB2X && IA2X > IC2X) {
                    result = new FeatureFlowDTO(pole.getFaultTime(), aH1hz, aH2hz, IA2X);
                } else if (IB2X > IA2X && IB2X > IC2X) {
                    result = new FeatureFlowDTO(pole.getFaultTime(), bH1hz, bH2hz, IB2X);
                } else if (IC2X > IA2X && IC2X > IB2X) {
                    result = new FeatureFlowDTO(pole.getFaultTime(), cH1hz, cH2hz, IC2X);
                }
                break;
            }
        }

        return result;
    }


    /**
     * 负荷波动判断
     *
     * @param faultWaves       The list of fault waves in the system.
     * @param hyConfigProperty The system's configuration properties.
     * @return boolean Indicates whether load fluctuation is detected.
     */
    public static Boolean loadFluctuationDetection(List<FaultWave> faultWaves, HyConfigProperty hyConfigProperty) {
        // Filter out three-phase current poles
        List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(faultWaves);

        // Get distinct pole IDs
        List<String> poleIds = threePhaseCurrents.stream()
                .map(FaultIdentifyPoleDTO::getPoleId)
                .distinct()
                .collect(Collectors.toList());

        // Filter out poles with both three-phase currents and voltages
        List<FaultIdentifyPoleDTO> threePhaseCurrentsAndVoltages =
                CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(poleIds, faultWaves);

        // TODO Define the minimum current required for load fluctuation
        double IMIN = hyConfigProperty.getConstant().getDeviceSampleRate();

        // Detect load fluctuation by examining each pole
        return threePhaseCurrentsAndVoltages.stream().anyMatch(pole -> {
            // Calculate current and voltage jumps for each phase
            double IAJMP = FrequencyCharacterCalculateUtil.IJMP(pole.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterCalculateUtil.IJMP(pole.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterCalculateUtil.IJMP(pole.getCPhaseCurrentData());

            // Calculate the 10th harmonic for each phase
            double IA10 = FrequencyCharacterCalculateUtil.I10(pole.getAPhaseCurrentData());
            double IB10 = FrequencyCharacterCalculateUtil.I10(pole.getBPhaseCurrentData());
            double IC10 = FrequencyCharacterCalculateUtil.I10(pole.getCPhaseCurrentData());

            // Calculate the voltage jumps for each phase
            double UAJMP = FrequencyCharacterCalculateUtil.UJMP(pole.getAPhaseVoltageData());
            double UBJMP = FrequencyCharacterCalculateUtil.UJMP(pole.getBPhaseVoltageData());
            double UCJMP = FrequencyCharacterCalculateUtil.UJMP(pole.getCPhaseVoltageData());

            // Calculate the 1st harmonic for each phase
            double IA1 = FrequencyCharacterCalculateUtil.I1(pole.getAPhaseCurrentData());
            double IB1 = FrequencyCharacterCalculateUtil.I1(pole.getBPhaseCurrentData());
            double IC1 = FrequencyCharacterCalculateUtil.I1(pole.getCPhaseCurrentData());

            // Check the load fluctuation conditions
            return (((IAJMP < 0 && ICJMP < 0 && IBJMP < 0) && (IA10 > IMIN && IB10 > IMIN && IC10 > IMIN))
                    || ((IAJMP > 0 && ICJMP > 0 && IBJMP > 0) && (IA10 > IA1 && IB10 > IB1 && IC10 > IC1)))
                    && (UBJMP < 0 && UAJMP < 0 && UCJMP < 0);
        });
    }

    /**
     * 负荷波动判断
     *
     * @param faultWaves       The list of fault waves in the system.
     * @param hyConfigProperty The system's configuration properties.
     * @return boolean Indicates whether load fluctuation is detected.
     */
    public static FeatureUndulateDTO loadFluctuationDetection2(Set<FaultWave> faultWaves, HyConfigProperty hyConfigProperty) {

        List<FaultWave> faultWaveList = new ArrayList<>(faultWaves);

        // Filter out three-phase current poles
        List<FaultIdentifyPoleDTO> threePhaseCurrents = CommonAlgorithmUtil.filterThreePhaseCurrentPole(faultWaveList);

        // Get distinct pole IDs
        List<String> poleIds = threePhaseCurrents.stream()
                .map(FaultIdentifyPoleDTO::getPoleId)
                .distinct()
                .collect(Collectors.toList());

        // Filter out poles with both three-phase currents and voltages
        List<FaultIdentifyPoleDTO> threePhaseCurrentsAndVoltages =
                CommonAlgorithmUtil.filterThreePhaseCurrentAndVoltagePole(poleIds, faultWaveList);

        // TODO Define the minimum current required for load fluctuation
        double IMIN = hyConfigProperty.getConstant().getDeviceSampleRate();

        FeatureUndulateDTO result = null;
        for (FaultIdentifyPoleDTO pole : threePhaseCurrentsAndVoltages) {

            // Calculate current and voltage jumps for each phase
            double IAJMP = FrequencyCharacterCalculateUtil.IJMP(pole.getAPhaseCurrentData());
            double IBJMP = FrequencyCharacterCalculateUtil.IJMP(pole.getBPhaseCurrentData());
            double ICJMP = FrequencyCharacterCalculateUtil.IJMP(pole.getCPhaseCurrentData());

            // Calculate the 10th harmonic for each phase
            double IA10 = FrequencyCharacterCalculateUtil.I10(pole.getAPhaseCurrentData());
            double IB10 = FrequencyCharacterCalculateUtil.I10(pole.getBPhaseCurrentData());
            double IC10 = FrequencyCharacterCalculateUtil.I10(pole.getCPhaseCurrentData());

            // Calculate the voltage jumps for each phase
            double UAJMP = FrequencyCharacterCalculateUtil.UJMP(pole.getAPhaseVoltageData());
            double UBJMP = FrequencyCharacterCalculateUtil.UJMP(pole.getBPhaseVoltageData());
            double UCJMP = FrequencyCharacterCalculateUtil.UJMP(pole.getCPhaseVoltageData());

            // Calculate the 1st harmonic for each phase
            double IA1 = FrequencyCharacterCalculateUtil.I1(pole.getAPhaseCurrentData());
            double IB1 = FrequencyCharacterCalculateUtil.I1(pole.getBPhaseCurrentData());
            double IC1 = FrequencyCharacterCalculateUtil.I1(pole.getCPhaseCurrentData());

            if ((((IAJMP < 0 && ICJMP < 0 && IBJMP < 0) && (IA10 > IMIN && IB10 > IMIN && IC10 > IMIN))
                    || ((IAJMP > 0 && ICJMP > 0 && IBJMP > 0) && (IA10 > IA1 && IB10 > IB1 && IC10 > IC1)))
                    && (UBJMP < 0 && UAJMP < 0 && UCJMP < 0)) {
                List<Double> changeCurrList = new ArrayList<>();
                changeCurrList.add(IA10 - IA1);
                changeCurrList.add(IB10 - IB1);
                changeCurrList.add(IC10 - IC1);

                result = new FeatureUndulateDTO();
                result.setVariationTime(pole.getFaultTime());
                result.setChangeCurr(Collections.max(changeCurrList));
                break;
            }
        }
        // Detect load fluctuation by examining each pole
        return result;
    }


    // 私有方法==========================================================================================================

    /**
     * 基于基波频率的判断方式检验工频电流/工频电压的有效性
     *
     * @return 是否为故障波形
     */
    // fixme
    public static boolean checkFundamentalFrequency(double[] signalData, HyConfigProperty hyConfigProperty) {
        double maxSignalThreshold = hyConfigProperty.getConstant().getFrequencyMaxThreshold();
        long frequencySamplingRate = hyConfigProperty.getConstant().getDeviceSampleRate();

        double maxSignalValue = Arrays.stream(signalData).map(Math::abs).max().orElse(0.0);
        if (maxSignalValue > maxSignalThreshold) return false;

        // Get a low-pass filter with a cutoff frequency of 1K.
        double[] lowPassFilter = {0.00149176912699628, 0.00113338606406803, 0.000286402296623436, -0.00169769035032670,
                -0.00517520815333871, -0.00964292315834651, -0.0134164361793712, -0.0137898593712167, -0.00773459964570550,
                0.00703454548213997, 0.0309850620686805, 0.0621043298912804, 0.0959667403435662, 0.126609163946671,
                0.148003498647870, 0.155683637980818, 0.148003498647870, 0.126609163946671, 0.0959667403435662, 0.0621043298912804,
                0.0309850620686805, 0.00703454548213997, -0.00773459964570550, -0.0137898593712167, -0.0134164361793712,
                -0.00964292315834651, -0.00517520815333871, -0.00169769035032670, 0.000286402296623436, 0.00113338606406803,
                0.00149176912699628};
        double[] filteredSignal = FFT.computeConvolution(signalData, lowPassFilter);

        double mean = Arrays.stream(filteredSignal).average().orElse(0.0);
        double stdDev = Math.sqrt(Arrays.stream(filteredSignal).map(i -> i - mean).map(i -> i * i).average().orElse(0.0));
        double transientThreshold = 3 * stdDev;

        double[] transientRemovedSignal = Arrays.stream(filteredSignal).filter(i -> Math.abs(i - mean) < transientThreshold).toArray();

        Complex[] complexSignalForFFT = new Complex[4096];
        int complexSignalLength = Math.min(transientRemovedSignal.length - 40, 4096);

        IntStream.range(0, complexSignalLength).forEach(i -> complexSignalForFFT[i] = new Complex(transientRemovedSignal[i + 20], 0));
        IntStream.range(complexSignalLength, 4096).forEach(i -> complexSignalForFFT[i] = new Complex(0, 0));

        Complex[] fftResult = fft(complexSignalForFFT);
        double normalizationFactor = 2.0 / (transientRemovedSignal.length - 40);

        int peakFrequencyIndex = IntStream.range(1, fftResult.length / 2 + 1)
                .reduce(0, (currentMaxIndex, i)
                        -> fftResult[i].abs() * normalizationFactor > fftResult[currentMaxIndex].abs() * normalizationFactor ? i : currentMaxIndex);

        double maxFrequencyHz = (double) frequencySamplingRate / 4096 * peakFrequencyIndex;

        return maxFrequencyHz < 52 && 48 < maxFrequencyHz;
    }



    /**
     * 基于相电流突变量的判断方式检验工频电流/工频电压的有效性(是否是故障波形)
     *
     * @return 是否为故障波形
     */
    private static boolean checkPhaseCurrentAbruptChange(double[] data, HyConfigProperty hyConfigProperty) {
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

    private static Double[] fftGetSpecificFrequencies(Double[] freqWaveData, long deviceSampleRate) {
        // Convert freqWaveData into a Complex array
        Complex[] complexInput = new Complex[freqWaveData.length];
        for (int i = 0; i < freqWaveData.length; i++) {
            complexInput[i] = new Complex(freqWaveData[i], 0);
        }

        // Perform FFT
        Complex[] fftResults = fft(complexInput);

        // Calculate indices corresponding to the frequencies
        int index1 = (int) ((double) 50 / (double) deviceSampleRate * freqWaveData.length);
        int index2 = (int) ((double) 100 / (double) deviceSampleRate * freqWaveData.length);

        // Extract 50Hz and 100Hz components
        double magnitude50Hz = fftResults[index1].abs();
        double magnitude100Hz = fftResults[index2].abs();

        return new Double[]{magnitude50Hz, magnitude100Hz};
    }


}
