package com.hy.biz.dataAnalysis.faultLocationAlgorithm;

import com.hy.biz.dataAnalysis.dto.FaultLocalizationAnalysisResult;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.extraAlgorithm.ExtraAlgorithmUtil;
import com.hy.config.HyConfigProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 故障定位：采用双端定位算法
 */
@Slf4j
@Component
public class FaultLocationAlgorithm {

    private final String NANO_PATTERN = "yyyy:MM:dd HH:mm:ss.SSSSSSSSS";

    private final HyConfigProperty hyConfigProperty;

    public FaultLocationAlgorithm(HyConfigProperty hyConfigProperty) {
        this.hyConfigProperty = hyConfigProperty;
    }

    public Optional<FaultLocalizationAnalysisResult> locate(Set<FaultWave> faultWaves) {

        List<FaultWave> validWaves = getValidWaves(faultWaves);  // 校验波形

        List<FaultWave> sortedWaves = sortByPhaseAndHeadTime(validWaves);  // 按照波形相位、起始时间排序

        Map<Integer, List<FaultWave>> phaseSortedWaveMap = createPhaseFaultWaveMap(sortedWaves);  // 将同一线路下不同相位的波形数据分组

        return computeFaultDist(phaseSortedWaveMap)  // 计算故障距离
                .map(minPair -> {
                    FaultLocalizationAnalysisResult result = new FaultLocalizationAnalysisResult();
                    result.setDistanceBetweenStations(minPair.getL2() + minPair.getL3());
                    result.setNearestPoleId(minPair.getNearestPoleId());
                    return result;
                });
    }


    //===========================private=========================private=============================private================================private===================


    private List<FaultWave> getValidWaves(Set<FaultWave> faultWaveSet) {
        if (faultWaveSet.isEmpty()) throw new RuntimeException("故障波形集合为空");

        return faultWaveSet.stream()
                .filter(faultWave -> {
                    int waveType = faultWave.getWaveType();
                    switch (waveType) {
                        case 1:
                            return ExtraAlgorithmUtil.isValidTravellingWave(faultWave.getData());
                        case 3:
                        case 5:
                            return ExtraAlgorithmUtil.isValidPowerFreqCurrentOrVoltage(faultWave.getData(), hyConfigProperty);
                        default:
                            throw new RuntimeException("未知波形数据类型");
                    }
                }).collect(Collectors.toList());
    }

    private List<FaultWave> sortByPhaseAndHeadTime(List<FaultWave> validWaves) {
        if (validWaves.isEmpty()) throw new RuntimeException("无通过校验的故障波形");
        return validWaves.stream().sorted(Comparator.comparing(FaultWave::getPhase)
                        .thenComparing(FaultWave::getHeadTime))
                .collect(Collectors.toList());
    }

    private Map<Integer, List<FaultWave>> createPhaseFaultWaveMap(List<FaultWave> sortedWaves) {
        return sortedWaves.stream().collect(Collectors.groupingBy(FaultWave::getPhase));
    }

    private Optional<DevicePairDistance> computeFaultDist(Map<Integer, List<FaultWave>> phaseSortedWaveMap) {
        List<DevicePairDistance> allPairs = new ArrayList<>();
        for (List<FaultWave> phaseSortedWaves : phaseSortedWaveMap.values()) {
            Optional<DevicePairDistance> optionalPair = analyzeFaultWaveByPhase(phaseSortedWaves);  //分析故障，生成结果
            optionalPair.ifPresent(allPairs::add);
        }
        // TODO 在所有的L2、L3中找到采集的两台设备中按照距离间隔最近的这两台计算出来的L2、L3
        return allPairs.stream().min(Comparator.comparing(pair -> pair.getL2() + pair.getL3()));
    }

    private Optional<DevicePairDistance> analyzeFaultWaveByPhase(List<FaultWave> phaseSortedWaves) {
        // 定位基点
        FaultWave basePointWave = phaseSortedWaves.get(0);
        Boolean basePointAbsolute = basePointWave.getAbsolute();

        // 定位参照点
        FaultWave referencePointWave = phaseSortedWaves.stream()
                .filter(phaseSortedWave -> !Objects.equals(phaseSortedWave.getAbsolute(), basePointAbsolute))
                .min(Comparator.comparing(FaultWave::getHeadTime))
                .orElse(null);
        if (referencePointWave == null) throw new RuntimeException("无法定位参照点");

        double averageSpeed = calculateAvgSpeed(phaseSortedWaves, basePointAbsolute); //// Calculate wave speed in m/ns
        // 计算L2与L3
        double L = Math.abs(basePointWave.getDistanceToHeadStation() - referencePointWave.getDistanceToHeadStation());
        double L2 = computeLength(basePointWave, referencePointWave, averageSpeed, L, false);
        double L3 = computeLength(basePointWave, referencePointWave, averageSpeed, L, true); // Length in m
        // 校验L2与L3
        if (L2 > L || L3 < 0) return Optional.empty();

        // 创建DevicePairDistance对象
        DevicePairDistance pair = new DevicePairDistance();
        pair.setL2(L2);
        pair.setL3(L3);
        long nearestPoleId = Long.parseLong(
                basePointWave.getDistanceToHeadStation() > referencePointWave.getDistanceToHeadStation() ?
                        referencePointWave.getPoleId() : basePointWave.getPoleId());
        pair.setNearestPoleId(nearestPoleId);

        return Optional.of(pair);
    }

    private double calculateAvgSpeed(List<FaultWave> phaseSortedWaves, Boolean basePointAbsolute) {
        // 找出极性相同的故障波形集合
        List<FaultWave> sameAbsoluteWaves = phaseSortedWaves.stream()
                .filter(faultWave -> Objects.equals(faultWave.getAbsolute(), basePointAbsolute))
                .collect(Collectors.toList());

        List<FaultWave> diffAbsoluteWaves = phaseSortedWaves.stream()
                .filter(faultWave -> !Objects.equals(faultWave.getAbsolute(), basePointAbsolute))
                .collect(Collectors.toList());

        List<Double> waveSpeeds = new ArrayList<>();
        if (sameAbsoluteWaves.size() >= 2)
            addValidatedSpeedToList(waveSpeeds, sameAbsoluteWaves);
        if (diffAbsoluteWaves.size() >= 2)
            addValidatedSpeedToList(waveSpeeds, diffAbsoluteWaves);

        return waveSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(hyConfigProperty.getConstant().getSpeed());
    }

    private void addValidatedSpeedToList(List<Double> waveSpeeds, List<FaultWave> waves) {
        for (int i = 0; i < waves.size() - 2; i += 2) {
            FaultWave wave1 = waves.get(i);
            FaultWave wave2 = waves.get(i + 1);

            double distance = Math.abs(wave1.getDistanceToHeadStation() - wave2.getDistanceToHeadStation());
            if (distance > 50000) continue;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(NANO_PATTERN);
            LocalDateTime time1 = LocalDateTime.parse(wave1.getHeadTime(), formatter);
            LocalDateTime time2 = LocalDateTime.parse(wave2.getHeadTime(), formatter);

            double deltaT = Duration.between(time1, time2).toNanos() * 1e-3;

            double waveSpeed = distance / deltaT; // Wave speed in m/us

            if (waveSpeed >= 280 && waveSpeed <= 300)
                waveSpeeds.add(waveSpeed);
        }
    }

    private double computeLength(FaultWave basePointWave, FaultWave referencePointWave, double averageSpeed, double L, boolean isL3) {

        LocalDateTime t1 = LocalDateTime.parse(basePointWave.getHeadTime(), DateTimeFormatter.ofPattern(NANO_PATTERN));
        LocalDateTime t2 = LocalDateTime.parse(referencePointWave.getHeadTime(), DateTimeFormatter.ofPattern(NANO_PATTERN));
        double timeDiffInSeconds = Duration.between(t1, t2).toNanos() * 1e-3;

        return isL3 ? (L - averageSpeed * timeDiffInSeconds) / 2 : (L + averageSpeed * timeDiffInSeconds) / 2;
    }

    @Getter
    @Setter
    private static class DevicePairDistance {
        private double L2;
        private double L3;
        private long nearestPoleId;
    }


}