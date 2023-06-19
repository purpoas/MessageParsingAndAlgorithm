package com.hy.biz.dataAnalysis.faultLocationAlgorithm;

import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.FaultLocationAnalysisResult;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.extraAlgorithm.ExtraAlgorithmUtil;
import com.hy.biz.dataAnalysis.faultLocationAlgorithm.exception.FaultLocationException;
import com.hy.config.HyConfigProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.hy.biz.dataAnalysis.faultLocationAlgorithm.constants.FaultLocationConstants.*;

/**
 * ===========================
 * 故障定位算法（双端）          ｜
 * ===========================
 *
 * @author shiwentao
 * @package com.hy.biz.dataAnalysis.faultLocationAlgorithm
 * @create 2023-05-23 09:27
 **/
@Slf4j
@Service
public class FaultLocationAlgorithm {

    private final HyConfigProperty hyConfigProperty;

    public FaultLocationAlgorithm(HyConfigProperty hyConfigProperty) {
        this.hyConfigProperty = hyConfigProperty;
    }

    public Optional<FaultLocationAnalysisResult> locate(Set<FaultWave> faultWaves/*入参故障波形均处于同一线路上*/) {

        List<FaultWave> validWaves = getValidWaves(faultWaves);  // 校验波形

        List<FaultWave> sortedWaves = sortByPhaseAndHeadTime(validWaves);  // 按照波形相位、起始时间排序

        Map<Integer, List<FaultWave>> phaseSortedWaveMap = createPhaseFaultWaveMap(sortedWaves);  // 将同一线路下不同相位的波形数据分组

        return computeFaultLocation(phaseSortedWaveMap)  // 计算故障距离
                .map(minPair -> {
                    FaultLocationAnalysisResult result = new FaultLocationAnalysisResult();
                    result.setDistanceBetweenStations(minPair.getDistanceBetweenStations());
                    result.setNearestPoleId(minPair.getNearestPoleId());
                    result.setDistToNearestPole(Math.min(minPair.getL2(), minPair.getL3()));
                    return result;
                });
    }


    //===========================private=========================private=============================private================================private===================


    private List<FaultWave> getValidWaves(Set<FaultWave> faultWaveSet) {
        if (faultWaveSet.isEmpty()) throw new FaultLocationException(EMPTY_FAULT_WAVE_SET_ERROR);
        return faultWaveSet.stream()
                .filter(faultWave -> {
                    int waveType = faultWave.getWaveType();
                    double[] data = CommonAlgorithmUtil.shiftWave(faultWave.getData());
                    switch (waveType) {
                        case 1:
                            return ExtraAlgorithmUtil.isValidTravellingWave(data, hyConfigProperty.getConstant().getTravelThreshold());
                        case 3:
                        case 5:
                            return ExtraAlgorithmUtil.isValidPowerFreqCurrentOrVoltage(data, hyConfigProperty);
                        default:
                            throw new FaultLocationException(UNKNOWN_WAVEFORM_TYPE_ERROR);
                    }
                }).collect(Collectors.toList());
    }

    private List<FaultWave> sortByPhaseAndHeadTime(List<FaultWave> validWaves) {
        if (validWaves.isEmpty()) throw new FaultLocationException(NONE_VALIDATED_FAULT_WAVE_ERROR);
        return validWaves.stream().sorted(Comparator.comparing(FaultWave::getPhase)
                .thenComparing(FaultWave::getHeadTime))
                .collect(Collectors.toList());
    }

    private Map<Integer, List<FaultWave>> createPhaseFaultWaveMap(List<FaultWave> sortedWaves) {
        return sortedWaves.stream().collect(Collectors.groupingBy(FaultWave::getPhase));
    }

    private Optional<DevicePairDistance> computeFaultLocation(Map<Integer, List<FaultWave>> phaseSortedWaveMap) {
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
        if (referencePointWave == null) throw new FaultLocationException(FAIL_TO_LOCATE_REFERENCE_POINT_ERROR);

        double averageSpeed = calculateAvgSpeed(phaseSortedWaves, basePointAbsolute); // Calculate wave speed in m/ns
        // 计算L2与L3
        double L = Math.abs(basePointWave.getDistanceToHeadStation() - referencePointWave.getDistanceToHeadStation());
        double L2 = computeLength(basePointWave, referencePointWave, averageSpeed, L, false); // Length in m
        double L3 = computeLength(basePointWave, referencePointWave, averageSpeed, L, true); // Length in m
        // 校验L2与L3
        if (L2 > L || L3 < 0) return Optional.empty();

        // 创建DevicePairDistance对象
        DevicePairDistance pair = new DevicePairDistance();
        pair.setL3(L3);
        pair.setL2(L2);
        pair.setBasePointPoleId(basePointWave.getPoleId());
        pair.setReferencePoleId(referencePointWave.getPoleId());
        pair.setDistanceBetweenStations(L3 > L2 ? L2 + referencePointWave.getDistanceToHeadStation() : L3 + basePointWave.getDistanceToHeadStation());
        pair.setNearestPoleId(L3 > L2 ? referencePointWave.getPoleId() : basePointWave.getPoleId());

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
        private double L3;
        private double L2;
        private String basePointPoleId;
        private String referencePoleId;
        private String nearestPoleId;
        private double distanceBetweenStations;
    }


}