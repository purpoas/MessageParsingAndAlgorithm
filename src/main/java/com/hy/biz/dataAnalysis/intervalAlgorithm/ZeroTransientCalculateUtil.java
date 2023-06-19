package com.hy.biz.dataAnalysis.intervalAlgorithm;

import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultIdentifyPoleDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.dto.IntervalPoleAbsoluteDTO;
import com.hy.biz.dataAnalysis.typeAlgorithm.TypeCalculateUtil;
import com.hy.biz.util.ListUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZeroTransientCalculateUtil {

    /**
     * 零序暂态特征区间定位入口函数
     *
     * @param faultWaves
     * @return
     */
    public static AreaLocateDTO locateInterval(List<FaultWave> faultWaves) {

        // TODO 1.筛选主线故障波形，定位出主线故障区间 （零序暂态特征分析需要杆塔有三相电流才能进行分析）
        // TODO 2.计算主线故障区间两侧零序电流相关系数，系数绝对值是否大于0.9，是：故障位于主线上，返回故障区间，否：故障位于支线上
        // TODO 3.筛选出支线故障波形，按照各支线挨个进行分析，判断支线中是否存在主线故障区间大号测极性相反的杆塔，有则定位出支线故障区间并退出后续支线不再分析

        // 筛选主线故障波形
        List<FaultWave> mainLineWave = faultWaves.stream().filter(faultWave -> faultWave.getLineId().equals(faultWave.getMainLineId())).collect(Collectors.toList());

        List<FaultIdentifyPoleDTO> mainPoleList = CommonAlgorithmUtil.filterThreePhaseCurrentPole(mainLineWave);

        List<IntervalPoleAbsoluteDTO> mainLineIntervalList = new ArrayList<>();
        for (FaultIdentifyPoleDTO pole : mainPoleList) {
            // 合成零序电流
            double[] synthesisDatas = TypeCalculateUtil.synthesisZeroCurrent(pole.getAPhaseCurrentData(), pole.getBPhaseCurrentData(), pole.getCPhaseCurrentData());

            // 计算零序电流极性
            boolean absolute = TypeCalculateUtil.calculateZeroCurrentAbsolute(synthesisDatas);

            IntervalPoleAbsoluteDTO dto = new IntervalPoleAbsoluteDTO(pole.getLineId(), pole.getPoleId(), pole.getDistanceToHeadStation(), absolute);
            mainLineIntervalList.add(dto);
        }

        // 主线突变区间 存在突变区间或者只存在极性相同区间
        AreaLocateDTO mainAreaLocateDTO = lookIntervalByPoleSerial(mainLineIntervalList);

        if (mainAreaLocateDTO == null) return null;

        // 计算突变区间两侧零序电流相关系数
        double[] leftDatas = mainAreaLocateDTO.getHeadFaultWaveData();
        double[] rightDatas = mainAreaLocateDTO.getEndFaultWaveData();

        double leftParam = TypeCalculateUtil.calculateZeroCurrentCoefficient(leftDatas);
        double rightParam = TypeCalculateUtil.calculateZeroCurrentCoefficient(rightDatas);

        // 两侧相关系数 >= 0.9 ||如果主线不存在突变区间 故障在支线上
        if (leftParam >= 0.9 || rightParam >= 0.9 || ArrayUtils.isEmpty(mainAreaLocateDTO.getEndFaultWaveData())) {
            // TODO 故障发送在支线上

            // 筛选支线故障波形
            List<FaultWave> branchLineWave = faultWaves.stream().filter(faultWave -> !faultWave.getLineId().equals(faultWave.getMainLineId())).collect(Collectors.toList());

            // 按照支线名分类故障波形
            Map<String, List<FaultWave>> branchLineMap = ListUtil.convertListToMapList(branchLineWave, FaultWave::getLineId);

            // 根据支线挨个分析
            AreaLocateDTO branchAreaLocateDTO = null;
            for (String branchLine : branchLineMap.keySet()) {
                List<FaultWave> lineBranchLineWave = branchLineMap.get(branchLine);

                List<FaultIdentifyPoleDTO> branchPoleList = CommonAlgorithmUtil.filterThreePhaseCurrentPole(lineBranchLineWave);

                List<IntervalPoleAbsoluteDTO> branchLineIntervalList = new ArrayList<>();
                for (FaultIdentifyPoleDTO pole : branchPoleList) {
                    // 合成零序电流
                    double[] synthesisDatas = TypeCalculateUtil.synthesisZeroCurrent(pole.getAPhaseCurrentData(), pole.getBPhaseCurrentData(), pole.getCPhaseCurrentData());

                    // 计算零序电流极性
                    boolean absolute = TypeCalculateUtil.calculateZeroCurrentAbsolute(synthesisDatas);

                    IntervalPoleAbsoluteDTO dto = new IntervalPoleAbsoluteDTO(pole.getLineId(), pole.getPoleId(), pole.getDistanceToHeadStation(), absolute);
                    branchLineIntervalList.add(dto);
                }

                // 主线区间大号测杆塔极性 如果为空则为小号测杆塔极性
                boolean compareAbsolute = mainAreaLocateDTO.getEndFaultWaveAbsolute() == null ? mainAreaLocateDTO.getHeadFaultWaveAbsolute() : mainAreaLocateDTO.getEndFaultWaveAbsolute();
                // 筛选支线所有杆塔极性和主线大号测极性相同的数量
                long filterAmount = branchLineIntervalList.stream().filter(dto -> dto.getAbsolute() == compareAbsolute).count();

                if (filterAmount == branchLineIntervalList.size()) {
                    // 支线所有杆塔和主线大号测极性相同 ---> 继续支线进行分析
                } else {
                    // 定位故障区间 返回结果
                    branchAreaLocateDTO = lookIntervalByPoleSerial(branchLineIntervalList);
                    break;
                }
            }

            // 如果支线故障区间存在 则返回支线故障区间
            if (branchAreaLocateDTO != null) {
                return branchAreaLocateDTO;
            }
        }

        return mainAreaLocateDTO;
    }

    /**
     * 通过杆塔序号定位故障区间 ， 寻找突变区间
     *
     * @param intervalPoleAbsoluteDTOList
     * @return 起始杆塔 结束杆塔 以及杆塔距离起始变电站距离
     */
    private static AreaLocateDTO lookIntervalByPoleSerial(List<IntervalPoleAbsoluteDTO> intervalPoleAbsoluteDTOList) {

        // 如果波形不满足条件 默认生成左区间

        // 按照杆塔顺序排列
        intervalPoleAbsoluteDTOList = intervalPoleAbsoluteDTOList.stream().sorted(Comparator.comparing(IntervalPoleAbsoluteDTO::getDistanceToHeadStation)).collect(Collectors.toList());

        boolean mutantAbsolute = intervalPoleAbsoluteDTOList.get(0).getAbsolute();

        List<IntervalPoleAbsoluteDTO> mutantPoles = new ArrayList<>();
        for (int i = 1; i < intervalPoleAbsoluteDTOList.size(); i++) {
            IntervalPoleAbsoluteDTO absoluteDTO = intervalPoleAbsoluteDTOList.get(i);

            boolean flag = absoluteDTO.getAbsolute() && mutantAbsolute;

            if (!flag) {
                // 极性突变区间 取当前值以及前一个值
                mutantPoles.add(intervalPoleAbsoluteDTOList.get(i - 1));
                mutantPoles.add(absoluteDTO);
                break;
            }
        }

        if (CollectionUtils.isEmpty(mutantPoles)) {
            // 不存在极性突变区间 取故障位置最后一个极性为+的大号测 （大号测：故障杆塔到结束变电站）
            IntervalPoleAbsoluteDTO absoluteDTO = intervalPoleAbsoluteDTOList.stream().sorted(Comparator.comparing(IntervalPoleAbsoluteDTO::getDistanceToHeadStation).reversed()).filter(IntervalPoleAbsoluteDTO::getAbsolute).findFirst().orElse(null);
            if (absoluteDTO != null)
                return new AreaLocateDTO(absoluteDTO.getPoleId(), null, absoluteDTO.getDistanceToHeadStation(), null, absoluteDTO.getAbsolute(), null);
        } else {
            // 存在突变区间
            IntervalPoleAbsoluteDTO f1 = mutantPoles.get(0);
            IntervalPoleAbsoluteDTO f2 = mutantPoles.get(1);

            return new AreaLocateDTO(f1.getPoleId(), f2.getPoleId(), f1.getDistanceToHeadStation(), f2.getDistanceToHeadStation(), f1.getAbsolute(), f2.getAbsolute(), f1.getData(), f2.getData());
        }

        return null;
    }


}
