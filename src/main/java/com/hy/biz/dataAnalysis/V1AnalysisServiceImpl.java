package com.hy.biz.dataAnalysis;

import com.google.gson.JsonObject;
import com.hy.biz.cache.service.AlgorithmCacheManager;
import com.hy.biz.cache.service.GroundFaultCacheManager;
import com.hy.biz.dataAnalysis.delay.DelayTaskQueue;
import com.hy.biz.dataAnalysis.dto.*;
import com.hy.biz.dataAnalysis.featureAlgorithm.FaultFeatureUtil;
import com.hy.biz.dataAnalysis.intervalAlgorithm.IntervalAlgorithm;
import com.hy.biz.dataAnalysis.faultLocationAlgorithm.FaultLocationAlgorithm;
import com.hy.biz.dataAnalysis.typeAlgorithm.FaultIdentifyAlgorithmUtil;
import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PoleDTO;
import com.hy.biz.dataParsing.dto.WaveDataMessage;
import com.hy.biz.util.TimeUtil;
import com.hy.config.HyConfigProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;

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
        // 执行算法分析主要逻辑

        // TODO 1.查询该线路上次故障类型是否是接地 且瞬时型故障判断延时时间未完成 ---->> 是 ： 补充接地故障瞬时特性 否 ： 涌流、负荷波形判断

        // TODO 2.区间定位

        AreaLocateDTO areaLocateDTO = IntervalAlgorithm.analysis(faultWaves);

        // TODO 3.故障类型判断
        if (areaLocateDTO == null) return;
        FaultIdentifyDTO faultType = FaultIdentifyAlgorithmUtil.judge(faultWaves, areaLocateDTO);

        // TODO 4.故障特性计算
        JsonObject faultFeatureObject = FaultFeatureUtil.calculate(faultType, areaLocateDTO, faultWaves);

        // TODO 5.故障精确定位
        faultLocationAlgorithm.locate(faultWaves).ifPresent(result -> {
            // 对 result 执行相关操作
        });

        // TODO 6.工频故障特征量计算


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
