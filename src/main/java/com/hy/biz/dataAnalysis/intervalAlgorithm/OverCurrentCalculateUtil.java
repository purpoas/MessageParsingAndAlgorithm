package com.hy.biz.dataAnalysis.intervalAlgorithm;

import com.hy.config.AnalysisConstants;
import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.typeAlgorithm.FrequencyCharacterCalculateUtil;
import com.hy.biz.util.ListUtil;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class OverCurrentCalculateUtil {

    /**
     * 过流特征区间定位入口函数
     *
     * @param faultWaves
     * @return
     */
    public static AreaLocateDTO locateInterval(List<FaultWave> faultWaves) {

        // TODO 1.筛选主线故障波形，定位出主线故障区间
        // TODO 2.筛选出支线故障波形，按照各支线挨个进行分析，判断支线中是否存在主线故障区间小号测极性相反的杆塔，有则定位出支线故障区间并退出后续支线不再分析
        // TODO 3.判断是否有支线故障区间 有返回支线故障区间 无则返回主线故障区间

        // 筛选主线故障波形
        List<FaultWave> mainLineWave = faultWaves.stream().filter(faultWave -> faultWave.getLineId().equals(faultWave.getMainLineId())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(mainLineWave)) return null;

        mainLineWave.forEach(faultWave -> {
            double[] in = CommonAlgorithmUtil.shiftWave(faultWave.getData());

            double I5 = FrequencyCharacterCalculateUtil.calculateCyclicWavePH(in, 5, AnalysisConstants.CYCLE_WAVE_LENGTH);

            // I_5i≥Iset的杆塔标志为“+”极性，否则为“-”极性
            if (I5 >= AnalysisConstants.Iset) {
                faultWave.setAbsolute(true);
            } else {
                faultWave.setAbsolute(false);
            }
        });

        // 寻找主线突变区间
        AreaLocateDTO mainAreaLocateDTO = lookIntervalByPoleSerial(mainLineWave);

        // 主线突变区间没有结果 退出后续计算
        if (mainAreaLocateDTO == null) return null;

        // 筛选支线故障波形
        List<FaultWave> branchLineWave = faultWaves.stream().filter(faultWave -> !faultWave.getLineId().equals(faultWave.getMainLineId())).collect(Collectors.toList());

        // 按照支线名分类故障波形
        Map<String, List<FaultWave>> branchLineMap = ListUtil.convertListToMapList(branchLineWave, FaultWave::getLineId);

        // 根据支线挨个分析
        AreaLocateDTO branchAreaLocateDTO = null;

        // 主线小号测波形极性
        boolean mainHeadWaveAbsolute = mainAreaLocateDTO.getHeadFaultWaveAbsolute();

        for (String branchLineId : branchLineMap.keySet()) {
            List<FaultWave> lineBranchLineWave = branchLineMap.get(branchLineId);

            lineBranchLineWave.forEach(faultWave -> {
                double[] in = CommonAlgorithmUtil.shiftWave(faultWave.getData());

                double I5 = FrequencyCharacterCalculateUtil.calculateCyclicWavePH(in, 5, AnalysisConstants.CYCLE_WAVE_LENGTH);

                // I_5i≥Iset的杆塔标志为“+”极性，否则为“-”极性
                if (I5 >= AnalysisConstants.Iset) {
                    faultWave.setAbsolute(true);
                } else {
                    faultWave.setAbsolute(false);
                }
            });

            // 筛选出极性为+的故障波形
            FaultWave branchLineOpinionWave = lineBranchLineWave.stream().filter(faultWave -> mainHeadWaveAbsolute == faultWave.getAbsolute()).findFirst().orElse(null);

            if (branchLineOpinionWave != null) {
                // 如果支线存在+极性 证明故障发生在支线上
                branchAreaLocateDTO = lookIntervalByPoleSerial(lineBranchLineWave);
                break;
            }
        }

        if (branchAreaLocateDTO == null) {
            return mainAreaLocateDTO;
        } else {
            return branchAreaLocateDTO;
        }
    }


    /**
     * 通过杆塔序号定位故障区间 ， 寻找突变区间
     *
     * @param phaseWaves
     * @return 起始杆塔 结束杆塔 以及杆塔距离起始变电站距离
     */
    private static AreaLocateDTO lookIntervalByPoleSerial(List<FaultWave> phaseWaves) {

        // 如果波形不满足条件 默认生成左区间

        // 按照杆塔顺序排列
        phaseWaves = phaseWaves.stream().sorted(Comparator.comparing(FaultWave::getDistanceToHeadStation)).collect(Collectors.toList());

        boolean mutantAbsolute = phaseWaves.get(0).getAbsolute();
        List<FaultWave> intervalWaves = new ArrayList<>();
        for (int i = 1; i < phaseWaves.size(); i++) {
            FaultWave faultWave = phaseWaves.get(i);

            boolean flag = faultWave.getAbsolute() && mutantAbsolute;

            if (!flag) {
                // 极性突变区间 取当前值以及前一个值
                intervalWaves.add(phaseWaves.get(i - 1));
                intervalWaves.add(faultWave);
                break;
            }
        }

        if (CollectionUtils.isEmpty(intervalWaves)) {
            // 不存在极性突变区间 取故障位置最后一个极性为+的大号测 （大号测：故障杆塔到结束变电站）
            FaultWave headWave = phaseWaves.stream().sorted(Comparator.comparing(FaultWave::getDistanceToHeadStation).reversed()).filter(FaultWave::getAbsolute).findFirst().orElse(null);
            if (headWave != null)
                return new AreaLocateDTO(headWave.getPoleId(), null, headWave.getDistanceToHeadStation(), null, headWave.getAbsolute(), null);
        } else {
            // 存在突变区间
            FaultWave f1 = intervalWaves.get(0);
            FaultWave f2 = intervalWaves.get(1);

            return new AreaLocateDTO(f1.getPoleId(), f2.getPoleId(), f1.getDistanceToHeadStation(), f2.getDistanceToHeadStation(), f1.getAbsolute(), f2.getAbsolute());
        }

        return null;
    }


}
