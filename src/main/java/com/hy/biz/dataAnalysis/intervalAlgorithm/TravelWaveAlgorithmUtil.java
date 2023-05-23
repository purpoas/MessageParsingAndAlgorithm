package com.hy.biz.dataAnalysis.intervalAlgorithm;

import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TravelWaveAlgorithmUtil {

    /**
     * 行波波形区间定位入口函数
     *
     * @param faultWaves
     * @return
     */
    public static AreaLocateDTO locateInterval(List<FaultWave> faultWaves) {
        // TODO 按照起始时间和相位进行分类
        // A相
        List<FaultWave> aPhaseWaves = faultWaves.stream().filter(faultWave -> 1 == faultWave.getPhase()).sorted(Comparator.comparing(FaultWave::getHeadTime)).collect(Collectors.toList());
        // B相
        List<FaultWave> bPhaseWaves = faultWaves.stream().filter(faultWave -> 2 == faultWave.getPhase()).sorted(Comparator.comparing(FaultWave::getHeadTime)).collect(Collectors.toList());
        // C相
        List<FaultWave> cPhaseWaves = faultWaves.stream().filter(faultWave -> 3 == faultWave.getPhase()).sorted(Comparator.comparing(FaultWave::getHeadTime)).collect(Collectors.toList());

        // A相区间定位结果
        AreaLocateDTO aInterval = lookIntervalByPhase(aPhaseWaves);
        // B相区间定位结果
        AreaLocateDTO bInterval = lookIntervalByPhase(bPhaseWaves);
        // C相区间定位结果
        AreaLocateDTO cInterval = lookIntervalByPhase(cPhaseWaves);

        if (aInterval == null && bInterval == null && cInterval == null) return null;

        // TODO 对各相故障区间计算其交集
        List<AreaLocateDTO> intervalList = new ArrayList<>();
        if (aInterval != null) intervalList.add(aInterval);
        if (bInterval != null) intervalList.add(bInterval);
        if (cInterval != null) intervalList.add(cInterval);

        long sameAbsoluteIntervalAmount = intervalList.stream().filter(areaLocateDTO -> areaLocateDTO.getIntervalDistance() == 0D).count();

        // 如果极性相同故障区间数量 = 故障区间数量
        if (sameAbsoluteIntervalAmount == intervalList.size()) return null;

        AreaLocateDTO left = intervalList.stream().max(Comparator.comparing(AreaLocateDTO::getFaultHeadTowerDistanceToHeadStation)).get();
        AreaLocateDTO right = intervalList.stream().min(Comparator.comparing(AreaLocateDTO::getFaultEndTowerDistanceToHeadStation)).get();

        // 起始杆塔取离变电站距离最远的 结束杆塔取离变电站距离最近的
        if (left != right && right.getFaultEndTowerDistanceToHeadStation() - left.getFaultHeadTowerDistanceToHeadStation() > 0) {
            return new AreaLocateDTO(left.getFaultHeadTowerId(), right.getFaultEndTowerId(), left.getFaultHeadTowerDistanceToHeadStation(), right.getFaultEndTowerDistanceToHeadStation());
        }

        return null;
    }


    /**
     * 分相位定位故障区间
     *
     * @param phaseWaves
     * @return 起始杆塔 结束杆塔 以及杆塔距离起始变电站距离
     */
    private static AreaLocateDTO lookIntervalByPhase(List<FaultWave> phaseWaves) {
        if (CollectionUtils.isEmpty(phaseWaves) || phaseWaves.size() == 1) return null;

        long count = phaseWaves.stream().map(FaultWave::getAbsolute).distinct().count();

        if (count == 1) {
            // 故障波形极性都相同 返回起始时间最小故障波形

            FaultWave firstFaultWave = phaseWaves.get(0);

            return new AreaLocateDTO(firstFaultWave.getPoleId(), firstFaultWave.getPoleId(), firstFaultWave.getDistanceToHeadStation(), firstFaultWave.getDistanceToHeadStation());
        }

        boolean mutantAbsolute = phaseWaves.get(0).getAbsolute();
        List<FaultWave> intervalWaves = new ArrayList<>();
        for (int i = 1; i < phaseWaves.size(); i++) {
            FaultWave faultWave = phaseWaves.get(i);

            boolean flag = faultWave.getAbsolute() && mutantAbsolute;

            if (!flag) {
                // 极性突变区间 取当前值以及前两个值
                if (i - 2 >= 0) {
                    intervalWaves.add(phaseWaves.get(i - 2));
                }
                intervalWaves.add(phaseWaves.get(i - 1));
                intervalWaves.add(faultWave);
                break;
            }
        }

        // 对intervalWaves计算故障区间

        if (CollectionUtils.isEmpty(intervalWaves)) return null;


        AreaLocateDTO dto = null;
        if (intervalWaves.size() == 2) {
            // 排序
            intervalWaves = intervalWaves.stream().sorted(Comparator.comparing(FaultWave::getDistanceToHeadStation)).collect(Collectors.toList());

            FaultWave firstFaultWave = intervalWaves.get(0);
            FaultWave endFaultWave = intervalWaves.get(1);

            dto = new AreaLocateDTO(firstFaultWave.getPoleId(), endFaultWave.getPoleId(), firstFaultWave.getDistanceToHeadStation(), endFaultWave.getDistanceToHeadStation());


        } else if (intervalWaves.size() == 3) {
            FaultWave f1 = intervalWaves.get(0);
            FaultWave f2 = intervalWaves.get(1);
            FaultWave f3 = intervalWaves.get(2);

            if (f3.getDistanceToHeadStation() > f2.getDistanceToHeadStation() && f2.getDistanceToHeadStation() < f1.getDistanceToHeadStation()) {
                // 3是结束杆塔 2在1的左侧 1为故障起始杆塔
                dto = new AreaLocateDTO(f1.getPoleId(), f3.getPoleId(), f1.getDistanceToHeadStation(), f3.getDistanceToHeadStation());
            } else if (f3.getDistanceToHeadStation() > f2.getDistanceToHeadStation() && f2.getDistanceToHeadStation() > f1.getDistanceToHeadStation()) {
                // 3是结束杆塔 2在1的右侧 2为故障起始杆塔
                dto = new AreaLocateDTO(f2.getPoleId(), f3.getPoleId(), f2.getDistanceToHeadStation(), f3.getDistanceToHeadStation());
            } else if (f3.getDistanceToHeadStation() < f2.getDistanceToHeadStation() && f2.getDistanceToHeadStation() < f1.getDistanceToHeadStation()) {
                // 3是起始杆塔 2在1的左侧 2为故障结束杆塔
                dto = new AreaLocateDTO(f3.getPoleId(), f2.getPoleId(), f3.getDistanceToHeadStation(), f2.getDistanceToHeadStation());
            } else if (f3.getDistanceToHeadStation() < f2.getDistanceToHeadStation() && f2.getDistanceToHeadStation() > f1.getDistanceToHeadStation()) {
                // 3是结束杆塔 2在1的右侧 1为故障起始杆塔
                dto = new AreaLocateDTO(f3.getPoleId(), f1.getPoleId(), f3.getDistanceToHeadStation(), f1.getDistanceToHeadStation());
            }
        }


        return dto;
    }

}
