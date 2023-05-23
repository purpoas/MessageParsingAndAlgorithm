package com.hy.biz.dataAnalysis.commonAlgorithm;

import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用算法计算相关函数
 */
public class CommonAlgorithmUtil {


    /**
     * 计算相邻两个电流值之差，并计算电流突变量能量 E(k)
     *
     * @param data   波形数据
     * @param power  突变量次方
     * @param deltaK 德尔塔K
     * @return
     */
    public static List<Double> calculateCurrentMutationEnergy(Double[] data, int power, int deltaK) {
        List<Double> deltaI = new ArrayList<>();

        for (int i = 1; i < data.length; i++) {
            Double value = data[i] - data[i - 1];
            deltaI.add(value);
        }

        List<Double> E = new ArrayList<>();

        for (int i = 0; i < deltaI.size() - deltaK + 1; i++) {
            double value = 0d;
            for (int j = 0; j < deltaK; j++) {
                value += Math.pow(deltaI.get(i + j), power);
            }
            E.add(value);
        }

        return E;
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

        List<Integer> integerList = new ArrayList<>();
        Map<Integer, Double> integerDoubleMap = new HashMap<>();
        if (data.get(0) > data.get(1)) {
            integerList.add(0);
            integerDoubleMap.put(0, data.get(0));
        }
        for (int i = 1; i < data.size() - 1; i++) {
            if (data.get(i) > data.get(i - 1) && data.get(i) > data.get(i + 1)) {
                integerList.add(i);
                integerDoubleMap.put(i, data.get(i));
            }
        }

        List<Integer> delList = getRemoveIndexList(integerList, integerDoubleMap, kickPointRange);

        return integerList.stream().filter(integer -> !delList.contains(integer)).collect(Collectors.toList());
    }

    /**
     * 根据坐标集 以及坐标对应值 找出 最大值坐标附近40个点的坐标集合 即需要剔除的点位
     *
     * @param integerList
     * @param integerDoubleMap
     * @return
     */
    private static List<Integer> getRemoveIndexList(List<Integer> integerList, Map<Integer, Double> integerDoubleMap, int kickPointRange) {

        if (CollectionUtils.isEmpty(integerList) || integerList.size() == 1) return null;

        List<Integer> result = new ArrayList<>();

        // 找到最大值坐标 maxIndex
        Integer maxIndex = getMaxValueIndex(integerDoubleMap);

        integerDoubleMap.remove(maxIndex);

        // 找出当前index需要剔除的坐标集合
        List<Integer> removeList = deleteMaxIndex(maxIndex, integerList, integerDoubleMap, kickPointRange);

        result.addAll(removeList);

        // 根据现有map 生成新的坐标值集合
        Set<Integer> integers = integerDoubleMap.keySet();
        List<Integer> nextIndexes = new ArrayList<>(integers);

        List<Integer> tempList = getRemoveIndexList(nextIndexes, integerDoubleMap, kickPointRange);
        if (!CollectionUtils.isEmpty(tempList)) {
            result.addAll(tempList);
        }

        return result;

    }

    /**
     * 找最大值坐标点 即map集合中value值最大对应的key
     *
     * @param integerDoubleMap
     * @return
     */
    private static Integer getMaxValueIndex(Map<Integer, Double> integerDoubleMap) {
        Set<Integer> integerSet = integerDoubleMap.keySet();

        Double max = integerDoubleMap.get(integerSet.iterator().next());
        Integer maxIndex = integerSet.iterator().next();
        for (Integer integer : integerSet) {
            if (integerDoubleMap.get(integer) > max) {
                maxIndex = integer;
                max = integerDoubleMap.get(integer);
            }
        }

        return maxIndex;
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
    private static List<Integer> deleteMaxIndex(Integer maxIndex, List<Integer> integerList, Map<Integer, Double> integerDoubleMap, int kickPointRange) {
        List<Integer> removeList = new ArrayList<>();
        for (int i = 0; i < integerList.size(); i++) {
            if (Math.abs(integerList.get(i) - maxIndex) == 0) {
                // integerList 中包含 maxIndex 需要去除这种情况
                continue;
            }
            if (Math.abs(integerList.get(i) - maxIndex) <= kickPointRange) {
                // 移除
                integerDoubleMap.remove(integerList.get(i));
                // 新增
                removeList.add(integerList.get(i));
            }
        }
        return removeList;
    }

    // TODO 计算波形极值点坐标函数  -------------- END


}
