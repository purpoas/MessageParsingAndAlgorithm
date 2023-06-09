package com.hy.service.impl;

/**
 * ======================
 * 故障定位接口实现        ｜
 * ======================
 *
 * @author shiwentao
 * @package com.hy.service.impl
 * @create 2023/6/7 10:30
 **/
//@Service
//public class FaultLocationServiceImpl implements FaultLocationService {
//
//    private final FaultLocationAlgorithm faultLocationAlgorithm;
//
//    public FaultLocationServiceImpl(FaultLocationAlgorithm faultLocationAlgorithm) {
//        this.faultLocationAlgorithm = faultLocationAlgorithm;
//    }
//
//    @Override
//    public String locateFaultTower(long poleAId, long poleBId, double distanceBetweenTowers) {
//        // 首先，你需要获取两个塔的波形数据。假设你已经有了一个可以用来获取波形数据的服务，
//        // 你可以在这里调用那个服务。然后，你可以将获取到的波形数据传递给 locate 方法。
//
//        Set<FaultWave> faultWaves = getFaultWavesFromPole(poleAId, poleBId);
//
//        Optional<FaultLocalizationAnalysisResult> analysisResult = faultLocationAlgorithm.locate(faultWaves);
//
//        if (analysisResult.isPresent()) {
//            FaultLocalizationAnalysisResult result = analysisResult.get();
//            return String.format("%d号杆塔+%f米", result.getNearestPoleId(), result.getDistanceBetweenStations());
//        } else {
//            throw new RuntimeException("无法定位故障杆塔");
//        }
//    }
//
//
//}
