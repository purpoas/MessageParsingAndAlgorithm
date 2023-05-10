package com.hy.biz.dataAnalysis;

import com.hy.biz.dataAnalysis.dto.FaultWave;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public interface DataAnalysisService {

    /**
     * 算法分析入口函数，对故障波形进行故障分析 (根据诊断分析流程而定)
     *
     * @param faultWaves
     */
    public void analysis(Set<FaultWave> faultWaves);


    /**
     * 执行算法任务生成/更新处理函数
     * @param deviceCode
     * @param wave
     */
    public void algorithmTask(String deviceCode, String wave);


}
