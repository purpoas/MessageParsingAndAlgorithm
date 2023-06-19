package com.hy.service.impl;

import com.hy.biz.dataAnalysis.dto.FaultLocationAnalysisResult;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.faultLocationAlgorithm.FaultLocationAlgorithm;
import com.hy.service.FaultLocationService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * ======================
 * 故障定位接口实现        ｜
 * ======================
 *
 * @author shiwentao
 * @package com.hy.service.impl
 * @create 2023/6/7 10:30
 **/
@Service
public class FaultLocationServiceImpl implements FaultLocationService {

    private final FaultLocationAlgorithm faultLocationAlgorithm;

    public FaultLocationServiceImpl(FaultLocationAlgorithm faultLocationAlgorithm) {
        this.faultLocationAlgorithm = faultLocationAlgorithm;
    }

    @Override
    public String locateFaultTower(long poleAId, long poleBId, double distanceBetweenTowers) {
        Set<FaultWave> faultWaves = getFaultWavesFromPole(poleAId, poleBId);

        Optional<FaultLocationAnalysisResult> analysisResult = faultLocationAlgorithm.locate(faultWaves);

        if (analysisResult.isPresent()) {
            FaultLocationAnalysisResult result = analysisResult.get();
            return String.format("%d号杆塔+%f米", result.getNearestPoleId(), result.getDistanceBetweenStations());
        } else
            throw new RuntimeException("无法定位故障杆塔");
    }


    //===========================private===========================private===========================private===========================


    private Set<FaultWave> getFaultWavesFromPole(long poleAId, long poleBId) {
        return null;
    }


}
