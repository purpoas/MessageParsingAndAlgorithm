package com.hy.biz.dataAnalysis.intervalAlgorithm;

import com.hy.biz.dataAnalysis.dto.AreaLocateDTO;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.typeAlgorithm.TypeAlgorithmUtil;
import com.hy.biz.util.ListUtil;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZeroTransientAlgorithmUtil {

    /**
     * 零序暂态特征区间定位入口函数
     *
     * @param faultWaves
     * @return
     */
    public static AreaLocateDTO locateInterval(List<FaultWave> faultWaves) {

        // 零序暂态特征分析需要杆塔有三相电流才能进行分析

        Map<String, List<FaultWave>> poleMap = ListUtil.convertListToMapList(faultWaves, FaultWave::getPoleId);

        for (String poleId : poleMap.keySet()) {

            List<FaultWave> poleFaultWaves = poleMap.get(poleId);

            long phaseAmount = poleFaultWaves.stream().map(FaultWave::getPhase).distinct().count();

            if (phaseAmount >= 3) {
                // 三相电流才进行分析

            }
        }

        return null;
    }

}
