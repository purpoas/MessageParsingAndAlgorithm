package com.hy.biz.dataAnalysis.commonAlgorithm;

import com.hy.biz.dataAnalysis.dto.FaultIdentifyPoleDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataParsing.constants.MessageType;
import com.hy.biz.util.ListUtil;
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
     *
     * @param data
     * @return
     */
    public static double[] shiftWave(String data) {
        return Stream.of(data.split(","))
                .map(Double::valueOf)
                .toArray();
    }

    /**
     * 计算相邻两个电流值之差，并计算电流突变量能量 E(k)
     *
     * @param data   波形数据
     * @param power  突变量次方
     * @param deltaK 德尔塔K
     * @return
     */
    public static List<Double> calculateCurrentMutationEnergy(Double[] data, int power, int deltaK) {
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
     *
     * @param faultWaveList 故障波形
     * @return
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
     * <p>
     * 用筛选出三相电流的故障波形集合进行过了
     *
     * @param faultWaveList 故障波形
     * @return
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
     *
     * @param data           波形内容
     * @param kickPointRange 剔除极值点坐标附近点的范围
     * @return
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
     *
     * @param integerList
     * @param integerDoubleMap
     * @return
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
     *
     * @param integerDoubleMap
     * @return
     */
    private static Integer getMaxValueIndex(Map<Integer, Double> indexToValueMap) {
        return Collections.max(indexToValueMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }



    /**
     * 根据最大值坐标点 、坐标集合 、 坐标map 找出需要剔除的点位
     *
     * @param maxIndex
     * @param integerList
     * @param integerDoubleMap
     * @param kickPointRange   剔除极值点坐标附近点的范围
     * @return 剔除坐标点集合
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


}
