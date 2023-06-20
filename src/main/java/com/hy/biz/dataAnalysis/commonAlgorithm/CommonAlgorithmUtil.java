package com.hy.biz.dataAnalysis.commonAlgorithm;

import com.hy.config.AnalysisConstants;
import com.hy.biz.dataAnalysis.algorithmUtil.Complex;
import com.hy.biz.dataAnalysis.algorithmUtil.FFT;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyPoleDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.typeAlgorithm.FrequencyCharacterCalculateUtil;
import com.hy.biz.dataParsing.constants.MessageType;
import com.hy.biz.util.ListUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 通用算法计算相关函数
 */
public class CommonAlgorithmUtil {

    /**
     * 转换波形 eg : 1,2,3 ---> [1.0,2.0,3.0]
     */
    public static double[] shiftWave(String data) {
        return Stream.of(data.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    /**
     * 进行4096点FFT
     *
     * @param data
     * @return
     */
    public static Complex[] fft4096(double[] data) {
        Complex[] complexes = new Complex[4096];
        for (int i = 0; i < complexes.length; i++) {
            if (i < data.length) {
                complexes[i] = new Complex(data[i], 0);
            } else {
                complexes[i] = new Complex(0, 0);
            }
        }
        return FFT.fft(complexes);
    }

    /**
     * 计算相邻两个电流值之差，并计算电流突变量能量 E(k)
     */
    public static List<Double> calculateCurrentMutationEnergy(double[] data, int power, int deltaK) {
        if (data == null || data.length < deltaK)
            throw new IllegalArgumentException("Data 数组不应为null 且元素数量不应小雨deltaK");

        List<Double> deltaI = IntStream.range(1, data.length)
                .mapToObj(i -> data[i] - data[i - 1])
                .collect(Collectors.toList());

        return IntStream.range(0, deltaI.size() - deltaK + 1)
                .mapToObj(i -> deltaI.subList(i, i + deltaK)
                        .stream()
                        .mapToDouble(a -> Math.pow(a, power))
                        .sum())
                .collect(Collectors.toList());
    }

    /**
     * 寻找筛选出杆塔下有三相电流的集合
     */
    public static List<FaultIdentifyPoleDTO> filterThreePhaseCurrentPole(List<FaultWave> faultWaveList) {
        List<FaultIdentifyPoleDTO> result = new ArrayList<>();

        // Group FaultWave by poleId
        Map<String, List<FaultWave>> poleMap = ListUtil.convertListToMapList(faultWaveList, FaultWave::getPoleId);

        Set<String> matchPoleSet = new HashSet<>();
        // Iterate over all poles
        for (String poleId : poleMap.keySet()) {
            List<FaultWave> faultWaves = poleMap.get(poleId);
            long[] phaseCounts = new long[3];
            // Count the number of FaultWave for each phase in one pass
            faultWaves.stream().filter(faultWave -> faultWave.getWaveType() == MessageType.FAULT_CURRENT)
                    .forEach(faultWave -> phaseCounts[faultWave.getPhase() - 1]++);
            // If all phases have at least one FaultWave, add pole to match set
            if (Arrays.stream(phaseCounts).allMatch(count -> count >= 1)) {
                matchPoleSet.add(poleId);
            }
        }

        // If no matching pole, return the result early
        if (matchPoleSet.isEmpty()) return result;

        // Iterate over all matching poles
        for (String poleId : matchPoleSet) {
            List<FaultWave> f = poleMap.get(poleId);

            double[][] phaseData = new double[3][];

            // Populate phase data for each FaultWave in one pass
            for (FaultWave faultWave : f) {
                if (faultWave.getWaveType() == MessageType.FAULT_CURRENT && faultWave.getPhase() >= 1 && faultWave.getPhase() <= 3) {
                    phaseData[faultWave.getPhase() - 1] = CommonAlgorithmUtil.shiftWave(faultWave.getData());
                }
            }

            result.add(new FaultIdentifyPoleDTO(f.get(0).getLineId(), f.get(0).getPoleId(), f.get(0).getDistanceToHeadStation()
                    , phaseData[0], phaseData[1], phaseData[2]));
        }

        return result;
    }


    /**
     * 寻找筛选出杆塔下有三相电流和三相电压的集合
     * 用筛选出三相电流的故障波形集合进行过了
     */
    public static List<FaultIdentifyPoleDTO> filterThreePhaseCurrentAndVoltagePole(List<String> poleIdList, List<FaultWave> faultWaveList) {
        List<FaultIdentifyPoleDTO> result = new ArrayList<>();

        // Group FaultWave by poleId
        Map<String, List<FaultWave>> poleMap = ListUtil.convertListToMapList(faultWaveList, FaultWave::getPoleId);

        Set<String> matchPoleSet = new HashSet<>();

        for (String poleId : poleIdList) {
            List<FaultWave> f = poleMap.get(poleId);

            if (f == null) continue;

            long[] phaseCounts = new long[6];

            // Count the number of FaultWave for each phase and type in one pass
            f.forEach(faultWave -> {
                if (faultWave.getWaveType() == MessageType.FAULT_VOLTAGE && faultWave.getPhase() >= 1 && faultWave.getPhase() <= 3) {
                    phaseCounts[faultWave.getPhase() - 1]++;
                }
            });

            // If all phases have at least one FaultWave of type FAULT_VOLTAGE, add pole to match set
            if (Arrays.stream(phaseCounts).limit(3).allMatch(count -> count >= 1)) {
                matchPoleSet.add(poleId);
            }
        }

        // If no matching pole, return the result early
        if (matchPoleSet.isEmpty()) {
            return result;
        }

        // Iterate over all matching poles
        for (String poleId : matchPoleSet) {
            List<FaultWave> f = poleMap.get(poleId);

            double[][] currentData = new double[3][];
            double[][] voltageData = new double[3][];

            // Populate current and voltage data for each FaultWave in one pass
            for (FaultWave faultWave : f) {
                if (faultWave.getWaveType() == MessageType.FAULT_CURRENT && faultWave.getPhase() >= 1 && faultWave.getPhase() <= 3) {
                    currentData[faultWave.getPhase() - 1] = CommonAlgorithmUtil.shiftWave(faultWave.getData());
                } else if (faultWave.getWaveType() == MessageType.FAULT_VOLTAGE && faultWave.getPhase() >= 1 && faultWave.getPhase() <= 3) {
                    voltageData[faultWave.getPhase() - 1] = CommonAlgorithmUtil.shiftWave(faultWave.getData());
                }
            }

            result.add(new FaultIdentifyPoleDTO(f.get(0).getLineId(), f.get(0).getPoleId(), f.get(0).getDistanceToHeadStation(),
                    currentData[0], currentData[1], currentData[2], voltageData[0], voltageData[1], voltageData[2]));
        }

        return result;
    }

    // TODO 计算波形极值点坐标函数  -------------- START

    /**
     * 计算波形极值点坐标集合
     */
    public static List<Integer> calculateWaveExtremePoint(List<Double> data, int kickPointRange) {
        // Replace integerDoubleMap and integerList with a single LinkedHashMap
        Map<Integer, Double> extremePointMap = new LinkedHashMap<>();
        // Add the first extreme point
        if (data.get(0) > data.get(1)) {
            extremePointMap.put(0, data.get(0));
        }
        // Find other extreme points
        for (int i = 1; i < data.size() - 1; i++) {
            if (data.get(i) > data.get(i - 1) && data.get(i) > data.get(i + 1)) {
                extremePointMap.put(i, data.get(i));
            }
        }
        // Get indices to be removed
        List<Integer> indicesToRemove = getRemoveIndexList(new ArrayList<>(extremePointMap.keySet()), extremePointMap, kickPointRange);
        // Get the list of extreme point indices, and remove unwanted indices
        List<Integer> extremePointIndices = new ArrayList<>(extremePointMap.keySet());
        extremePointIndices.removeAll(indicesToRemove);

        return extremePointIndices;
    }

    /**
     * 根据坐标集 以及坐标对应值 找出 最大值坐标附近40个点的坐标集合 即需要剔除的点位
     */
    private static List<Integer> getRemoveIndexList(List<Integer> indexList, Map<Integer, Double> indexToValueMap, int kickPointRange) {
        if (CollectionUtils.isEmpty(indexList) || indexList.size() == 1) return Collections.emptyList();

        // 找到最大值坐标 maxIndex
        Integer maxIndex = getMaxValueIndex(indexToValueMap);

        indexToValueMap.remove(maxIndex);

        // 找出当前index需要剔除的坐标集合
        List<Integer> currentRemovedIndices = deleteMaxIndex(maxIndex, indexList, indexToValueMap, kickPointRange);

        List<Integer> indicesToRemove = new ArrayList<>(currentRemovedIndices);

        // 根据现有map 生成新的坐标值集合
        List<Integer> nextIndexes = new ArrayList<>(indexToValueMap.keySet());

        indicesToRemove.addAll(getRemoveIndexList(nextIndexes, indexToValueMap, kickPointRange));

        return indicesToRemove;
    }

    /**
     * 找最大值坐标点 即map集合中value值最大对应的key
     */
    private static Integer getMaxValueIndex(Map<Integer, Double> indexToValueMap) {
        return Collections.max(indexToValueMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }


    /**
     * 根据最大值坐标点 、坐标集合 、 坐标map 找出需要剔除的点位
     */
    private static List<Integer> deleteMaxIndex(Integer maxIndex, List<Integer> indexList, Map<Integer, Double> indexToValueMap, int kickPointRange) {
        return indexList.stream()
                .filter(index -> {
                    int difference = Math.abs(index - maxIndex);
                    return difference > 0 && difference <= kickPointRange;
                })
                .peek(indexToValueMap::remove)
                .collect(Collectors.toList());
    }

    // TODO 计算波形极值点坐标函数  -------------- END

    /**
     * 在指定频率区间中 求极大值对应的幅值
     *
     * @param complexes fft变换后的数组
     * @param fftLength 进行fft变换的值 eg 1024 4096
     * @param sampRate  波形采样率
     * @param minHz     最小频率
     * @param maxHz     最大频率
     * @return
     */
    public static Double getMaxHzNew(Complex[] complexes, Integer fftLength, Integer sampRate, Integer minHz, Integer maxHz) {
        Double[] amplitude = new Double[complexes.length];

        for (int i = 0; i < complexes.length; i++) {
            // 将complexes转化为对应幅值数组
            amplitude[i] = complexes[i].abs() * 2 / fftLength;
        }

        // 计算最小频率对应的坐标点 最大频率对应的坐标点
        int i1 = minHz * fftLength / sampRate;
        int i2 = maxHz * fftLength / sampRate;

        // 找出 amplitude 中 i1 - i2 之间对应的最大值
        Integer maxIndex = i1 - 1;
        Double max = amplitude[maxIndex];

        for (int i = i1; i < i2; i++) {
            if (max < amplitude[i]) {
                max = amplitude[i];
                maxIndex = i;
            }
        }

        return max;
    }

    /**
     * 工频电流计算接地故障、过流故障 TODO 预判断
     *
     * @param faultWaves 故障波形
     * @return 0-未知  1-过流  2-接地
     */
    public static int judgeFrequencyCurrentWaveFaultType(List<FaultWave> faultWaves) {

        double Iset = AnalysisConstants.Iset;
        double I0set = AnalysisConstants.I0set;

        List<double[]> doubles = faultWaves.stream().map(FaultWave::getData).map(CommonAlgorithmUtil::shiftWave).collect(Collectors.toList());

        List<Double> I5s = doubles.stream().map(d -> FrequencyCharacterCalculateUtil.calculateCyclicWavePH(d, 5, AnalysisConstants.CYCLE_WAVE_LENGTH)).collect(Collectors.toList());

        Double overValue = I5s.stream().filter(aDouble -> aDouble >= Iset).findAny().orElse(null);

        // 过流故障
        if (overValue != null) return 1;

        // 满足该杆塔有三相故障电流
        List<FaultIdentifyPoleDTO> faultIdentifyPoles = CommonAlgorithmUtil.filterThreePhaseCurrentPole(faultWaves);

        List<Double> I0List = new ArrayList<>();
        for (FaultIdentifyPoleDTO pole : faultIdentifyPoles) {
            // 计算其合成零序电流
            I0List.add(FrequencyCharacterCalculateUtil.calculateZeroCurrent(pole.getAPhaseCurrentData(), pole.getBPhaseCurrentData(), pole.getCPhaseCurrentData()));
        }

        Double zeroValue = I0List.stream().filter(aDouble -> aDouble >= I0set).findAny().orElse(null);

        // 接地故障
        if (zeroValue != null) return 2;

        return 0;
    }




    /**
     * 零序电流极性计算  3.6.3 A
     *
     * @param data 波形内容
     * @return 零序电流极性
     */
    public static boolean calculateZeroCurrentAbsolute(double[] data) {
        int N = AnalysisConstants.CYCLE_WAVE_LENGTH;        //一个周波对应点位
        int pos = calculateZeroCurrentPosLocation(data);    //触发位置

        int prePoint = N / 4;
        int sufPoint = N / 2;

        int startIndex = Math.max(pos - prePoint, 0);
        int endIndex = Math.min(pos + sufPoint, data.length);

        // 数据处理后的原始波形
        double[] subData = new double[endIndex - startIndex + 1];
        System.arraycopy(data, startIndex, subData, 0, subData.length);

        // 计算突变量
        List<Double> q = CommonAlgorithmUtil.calculateCurrentMutationEnergy(subData, 3, 5);

        // 将Q(k)取绝对值得到E(k)
        List<Double> e = q.stream().map(Math::abs).collect(Collectors.toList());

        double eMax = Collections.max(e);

        // 寻找E(k)极值点坐标
        List<Integer> eMaxIndexList = CommonAlgorithmUtil.calculateWaveExtremePoint(e, 3);

        // 寻找E(k)极值点坐标集合中 第一个突变量能量大于等于0.2倍E(k)Max的点
        int firstIndex = 0;
        for (int i = 0; i < eMaxIndexList.size(); i++) {
            if (e.get(eMaxIndexList.get(i)) >= 0.2 * eMax) {
                firstIndex = eMaxIndexList.get(i);
                break;
            }
        }

        double qFirstMaxValue = q.get(firstIndex);

        if (qFirstMaxValue > 0) return true;

        return false;

    }

    /**
     * 零序电流相关系数计算  3.6.3 B
     *
     * @param data 波形内容
     * @return 零序电流极性
     */
    public static double calculateZeroCurrentCoefficient(double[] data) {

        if (ArrayUtils.isEmpty(data)) return 0.0;

        int N = AnalysisConstants.CYCLE_WAVE_LENGTH;        //一个周波对应点位
        int pos = calculateZeroCurrentPosLocation(data);    //触发位置

        int startIndex = Math.max(pos - N, 0);
        int endIndex = Math.min(pos + N, data.length);

        List<Double> xn = new ArrayList<>();
        List<Double> yn = new ArrayList<>();
        for (int i = startIndex; i < pos; i++) {
            xn.add(data[i]);
        }

        for (int i = pos; i < endIndex; i++) {
            yn.add(data[i]);
        }

        double xnMax = Collections.max(xn);
        double xnMin = Collections.min(xn);
        double ynMax = Collections.max(yn);
        double ynMin = Collections.min(yn);

        // 归一化
        xn = xn.stream().map(aDouble -> (2 * aDouble - xnMin) / (xnMax - xnMin) - 1).collect(Collectors.toList());
        yn = yn.stream().map(aDouble -> (2 * aDouble - ynMin) / (ynMax - ynMin) - 1).collect(Collectors.toList());

        double up = 0.0;
        for (int i = 0; i < xn.size(); i++) {
            up += xn.get(i) * yn.get(i);
        }

        double down = 0.0;
        for (int i = 0; i < xn.size(); i++) {
            down += xn.get(i) * xn.get(i) * yn.get(i) * yn.get(i);
        }

        return up / Math.sqrt(down);
    }

    /**
     * 零序电流触发位置Pos计算  3.6.3 A、B计算中使用通用方法
     *
     * @param data 波形内容
     * @return 触发位置
     */
    private static int calculateZeroCurrentPosLocation(double[] data) {
        int fpower = 12800; //设备采样率
        int threshold = 5;  //电流阈值
        int N = AnalysisConstants.CYCLE_WAVE_LENGTH;        //一个周波对应点位

        // 从2N+1点开始计算pos
        List<Double> deltaI = new ArrayList<>();
        for (int n = 2 * N; n < data.length; n++) {
            // FIXME 这里需要确定是n-N还是n-N-1
            double i = Math.abs(Math.abs(data[n] - data[n - N]) - Math.abs(data[n] - data[n - 2 * N]));
            deltaI.add(i);
        }

        // Pos是第一个大于触发阈值的点
        int pos = 0;
        for (int i = 0; i < deltaI.size(); i++) {
            if (deltaI.get(i) > threshold) {
                pos = i;
                break;
            }
        }

        // 校验Pos有效性
        if ((fpower / 50 * 7 / 2 >= pos || pos >= fpower / 50 * 9 / 2)) {
            pos = 1024;
        }

        return pos;
    }

}
