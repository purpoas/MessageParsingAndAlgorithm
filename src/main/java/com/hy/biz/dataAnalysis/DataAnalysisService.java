package com.hy.biz.dataAnalysis;

import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataResolver.dto.WaveDataMessage;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public interface DataAnalysisService {

    /**
     * 实现对故障波形的分析工作，诊断出故障结果，函数执行处理流程根据诊断分析流程而定
     *
     * @param faultWaves 故障波形
     */
    public void executeAlgorithmAnalysis(Set<FaultWave> faultWaves);


    /**
     * 根据设备上传波形创建算法任务，函数内部执行逻辑需要对波形进行有效性监测，通过有效性后进行算法任务创建逻辑执行
     *
     * @param waveDataMessage 报文类型
     */
    public void createAlgorithmTask(WaveDataMessage waveDataMessage);


}
