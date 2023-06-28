package com.hy.biz.dataAnalysis.delay;

import com.hy.biz.dataAnalysis.DataAnalysisService;
import com.hy.biz.dataAnalysis.dto.AlgorithmIdentify;
import com.hy.biz.dataAnalysis.dto.AlgorithmTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 延迟算法任务
 */
public class DelayAlgorithmTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(DelayAlgorithmTask.class);

    private AlgorithmIdentify algorithmIdentify;

    private AlgorithmTask algorithmTask;

    private DataAnalysisService dataAnalysisService;

    public DelayAlgorithmTask(AlgorithmIdentify algorithmIdentify, AlgorithmTask algorithmTask, DataAnalysisService dataAnalysisService) {
        this.algorithmIdentify = algorithmIdentify;
        this.algorithmTask = algorithmTask;
        this.dataAnalysisService = dataAnalysisService;
    }


    @Override
    public void run() {
        log.info("[DELAY] executeAlgorithmAnalysis ， AlgorithmIdentify ： {} , FaultWave size ： {}", algorithmIdentify.toString(), algorithmTask.getFaultWaveSet().size());

        // 执行算法任务
        dataAnalysisService.executeAlgorithmAnalysis(algorithmTask.getFaultWaveSet());
    }


}
