package com.hy.biz.dataAnalysis.intervalAlgorithm;

import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.typeAlgorithm.TypeCalculateUtil;
import com.hy.biz.dataParsing.constants.MessageType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 故障区间定位：
 * 1.行波极性区间定位
 * 2.接地故障采用零序暂态方法区间定位
 * 3.过流故障采用过流特征区间定位
 */
public class IntervalAlgorithm {

    /**
     * 区间定位入口函数
     *
     * @param faultWaves
     * @return
     */
    public static AreaLocateDTO analysis(Set<FaultWave> faultWaves) {

        AreaLocateDTO areaLocateDTO = null;

        // 筛选出行波波形
        List<FaultWave> travelWaveList = faultWaves.stream().filter(faultWave -> MessageType.TRAVELLING_WAVE_CURRENT == faultWave.getWaveType()).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(travelWaveList) && faultWaves.size() >= 2) {
            // 调用行波区间定位算法计算
            areaLocateDTO = TravelWaveCalculateUtil.locateInterval(travelWaveList);

            if (areaLocateDTO == null) {
                // 工频电流波形计算是否是接地故障还是过流故障

                List<FaultWave> frequencyWaveList = faultWaves.stream().filter(faultWave -> MessageType.FAULT_CURRENT == faultWave.getWaveType()).collect(Collectors.toList());

                if (CollectionUtils.isEmpty(frequencyWaveList)) return areaLocateDTO;

                // 故障类型判断
                int faultType = TypeCalculateUtil.judgeFrequencyCurrentWaveFaultType(frequencyWaveList);

                if (faultType == 0) return areaLocateDTO;

                if (faultType == 1) {
                    // 过流特征区间定位
                    areaLocateDTO = OverCurrentCalculateUtil.locateInterval(frequencyWaveList);
                } else if (faultType == 2) {
                    // 零序暂态特征区间定位
                    areaLocateDTO = ZeroTransientCalculateUtil.locateInterval(frequencyWaveList);
                }
            }
        }

        return areaLocateDTO;
    }


}
