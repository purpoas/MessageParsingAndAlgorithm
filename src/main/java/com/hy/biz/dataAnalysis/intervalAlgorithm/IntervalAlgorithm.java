package com.hy.biz.dataAnalysis.intervalAlgorithm;

import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.typeAlgorithm.TypeAlgorithmUtil;
import com.hy.biz.dataParsing.constants.MessageType;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            areaLocateDTO = TravelWaveAlgorithmUtil.locateInterval(travelWaveList);
            if (areaLocateDTO == null) {
                // 工频电流波形计算是否是接地故障还是过流故障

                List<FaultWave> frequencyWaveList = faultWaves.stream().filter(faultWave -> MessageType.FAULT_CURRENT == faultWave.getWaveType()).collect(Collectors.toList());

                if (CollectionUtils.isEmpty(frequencyWaveList)) return areaLocateDTO;

                // 故障类型判断
                int faultType = TypeAlgorithmUtil.judgeFrequencyCurrentWaveFaultType(frequencyWaveList);

                if (faultType == 0) return areaLocateDTO;

                if (faultType == 1) {
                    // 过流特征区间定位
                    areaLocateDTO = OverCurrentAlgorithmUtil.locateInterval(frequencyWaveList);
                } else if (faultType == 2) {
                    // 零序暂态特征区间定位
                    areaLocateDTO = ZeroTransientAlgorithmUtil.locateInterval(frequencyWaveList);
                }
            }
        }

        return areaLocateDTO;
    }


}
