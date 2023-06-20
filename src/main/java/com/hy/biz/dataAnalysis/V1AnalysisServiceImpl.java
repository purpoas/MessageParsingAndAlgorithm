package com.hy.biz.dataAnalysis;

import com.hy.biz.cache.service.AlgorithmCacheManager;
import com.hy.biz.cache.service.GroundFaultCacheManager;
import com.hy.config.AnalysisConstants;
import com.hy.biz.dataAnalysis.delay.DelayTaskQueue;
import com.hy.biz.dataAnalysis.dto.*;
import com.hy.biz.dataAnalysis.extraAlgorithm.ExtraAlgorithmUtil;
import com.hy.biz.dataAnalysis.featureAlgorithm.FaultFeatureAlgorithm;
import com.hy.biz.dataAnalysis.intervalAlgorithm.IntervalAlgorithm;
import com.hy.biz.dataAnalysis.faultLocationAlgorithm.FaultLocationAlgorithm;
import com.hy.biz.dataAnalysis.typeAlgorithm.FaultTypeAlgorithm;
import com.hy.biz.dataParsing.constants.MessageType;
import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PoleDTO;
import com.hy.biz.dataParsing.dto.WaveDataMessage;
import com.hy.biz.util.GsonUtil;
import com.hy.biz.util.TimeUtil;
import com.hy.config.HyConfigProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * v1版本算法实现
 */
@Component
@Slf4j
public class V1AnalysisServiceImpl implements DataAnalysisService {

    @Autowired
    private HyConfigProperty hyConfigProperty;

    @Autowired
    private FaultLocationAlgorithm faultLocationAlgorithm;

    @Autowired
    private DataPushService dataPushService;

    @Autowired
    private DelayTaskQueue taskQueue;

    @Autowired
    private AlgorithmCacheManager algorithmCacheManager;

    @Autowired
    private GroundFaultCacheManager groundFaultCacheManager;

    @Override
    public void executeAlgorithmAnalysis(Set<FaultWave> faultWaves) {
        if (CollectionUtils.isEmpty(faultWaves)) return;

        log.info("[ANALYSIS] 故障波形 size : {}", faultWaves.size());

        FaultAnalysisResultDTO result = null;

        // 故障线路
        String lineId = faultWaves.iterator().next().getLineId();

        // 故障时间
        String faultTime = faultWaves.stream().filter(faultWave -> faultWave.getWaveType() == MessageType.TRAVELLING_WAVE_CURRENT).map(FaultWave::getHeadTime).min(String::compareToIgnoreCase).orElse(null);
        if (StringUtils.isEmpty(faultTime)) {
            faultTime = faultWaves.stream().filter(faultWave -> faultWave.getWaveType() != MessageType.TRAVELLING_WAVE_CURRENT).map(FaultWave::getHeadTime).min(String::compareToIgnoreCase).orElse(null);
        }

        // 故障波形列表
        List<String> faultWaveIdList = faultWaves.stream().map(FaultWave::getWaveCode).collect(Collectors.toList());
        String faultWaveIds = StringUtils.join(faultWaveIdList, ",");

        // TODO 1.线路上次故障是否是接地故障判断

        // 线路上次接地故障
        FaultAnalysisResultDTO groundAnalysisResult = groundFaultCacheManager.get(lineId);
        if (groundAnalysisResult != null) {

            // 是： 补充接地故障瞬时性特性
            // 否： 涌流/负荷波动判断 先涌流后负荷
            FeatureFlowDTO featureFlowDTO = ExtraAlgorithmUtil.isSurge2(faultWaves, hyConfigProperty);

            if (featureFlowDTO != null) {
                result = new FaultAnalysisResultDTO();
                result.setFaultLineId(lineId);
                result.setFaultTime(faultTime);
                result.setFaultType(AnalysisConstants.FAULT_NATURE_FLASHY_FLOW);
                result.setFaultFeature(FaultFeatureAlgorithm.createFaultFeatureDescription(GsonUtil.getInstance().toJson(featureFlowDTO)));
                result.setFaultWaveSets(faultWaveIds);
            } else {
                FeatureUndulateDTO featureUndulateDTO = ExtraAlgorithmUtil.loadFluctuationDetection2(faultWaves, hyConfigProperty);
                if (featureUndulateDTO != null) {
                    result = new FaultAnalysisResultDTO();
                    result.setFaultLineId(lineId);
                    result.setFaultTime(faultTime);
                    result.setFaultType(AnalysisConstants.FAULT_NATURE_LOAD_UNDULATE);
                    result.setFaultFeature(FaultFeatureAlgorithm.createFaultFeatureDescription(GsonUtil.getInstance().toJson(featureUndulateDTO)));
                    result.setFaultWaveSets(faultWaveIds);
                }
            }

            if (result != null) {
                // 推送故障告警
                dataPushService.pushFaultAlarm(result);
                return;
            }
        }

        // TODO 2.区间定位

        AreaLocateDTO areaLocateDTO = IntervalAlgorithm.analysis(faultWaves);
        if (areaLocateDTO == null) return;

        // TODO 3.故障类型判断

        FaultIdentifyDTO faultType = FaultTypeAlgorithm.judge(faultWaves, areaLocateDTO);

        // TODO 4.故障特性计算
        String faultFeature = FaultFeatureAlgorithm.calculate(faultType, areaLocateDTO, faultWaves);

        result = new FaultAnalysisResultDTO();
        result.setFaultLineId(lineId);
        result.setFaultTime(faultTime);
        result.setFaultType(faultType.getFaultType());
        result.setFaultFeature(faultFeature);
        result.setFaultHeadPoleId(areaLocateDTO.getFaultHeadTowerId());
        result.setFaultEndPoleId(areaLocateDTO.getFaultEndTowerId());
        result.setFaultWaveSets(faultWaveIds);

        // TODO 5.故障精确定位
        FaultLocationAnalysisResult localizationResult = faultLocationAlgorithm.locate(faultWaves).orElse(null);
        if (localizationResult != null) {
            result.setNearestPoleId(String.valueOf(localizationResult.getNearestPoleId()));
            result.setDistToFaultPole(localizationResult.getDistToNearestPole());
        }

        // 推送故障告警
        dataPushService.pushFaultAlarm(result);
    }

    @Override
    public void createAlgorithmTask(WaveDataMessage waveDataMessage) {
        if (!waveDataMessage.isFault()) return;

        // 故障波形判断(如果是故障波形，完成故障波形上送) --> 算法唯一标识写入Cache中 --> 放入延迟队列

        DeviceDTO deviceDTO = dataPushService.findDeviceByCode(waveDataMessage.getDeviceCode());

        if (deviceDTO == null) return;

        PoleDTO poleDTO = dataPushService.getPoleByPoleId(deviceDTO.getLineId(), deviceDTO.getPoleId());

        if (poleDTO == null) return;

        // 生成FaultWave对象
        FaultWave faultWave = waveDataMessage.transform(deviceDTO, poleDTO);
        // 生成算法标识
        AlgorithmIdentify algorithmIdentify = new AlgorithmIdentify(String.valueOf(deviceDTO.getLineId()), TimeUtil.handleHeadTimeToTimestamp(waveDataMessage.getWaveStartTime()), hyConfigProperty.getAlgorithm().getDelayExecuteTime());
        // 从Cache中获取算法任务
        AlgorithmTask algorithmTask = algorithmCacheManager.get(algorithmIdentify);
        // 根据算法标识判断是Cache中否存在算法任务 存在将故障波形放入算法列表中，不存在创建算法任务放入Cache中
        if (algorithmTask == null) {
            AlgorithmTask algorithmTaskNew = AlgorithmTask.createDefaultObject(String.valueOf(deviceDTO.getLineId()), faultWave);
            algorithmCacheManager.put(algorithmIdentify, algorithmTaskNew);
            // 算法标识放入延迟队列中
            taskQueue.add(algorithmIdentify);
            log.info("[DELAY] {} 放入延迟队列", algorithmIdentify.toString());
        } else {
            algorithmTask.getFaultWaveSet().add(faultWave);
        }
    }

}
