package com.hy.biz.dataAnalysis.dto;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 算法任务-用于进行故障波形分析
 */
public class AlgorithmTask {

    // 线路ID
    private String lineId;

    // 故障波形集合
    private Set<FaultWave> faultWaveSet;

    // 生成时间
    private Instant createTime;

    private long timestamp = System.currentTimeMillis();


    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public Set<FaultWave> getFaultWaveSet() {
        return faultWaveSet;
    }

    public void setFaultWaveSet(Set<FaultWave> faultWaveSet) {
        this.faultWaveSet = faultWaveSet;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public synchronized static AlgorithmTask createDefaultObject(String lineId, FaultWave wave) {
        AlgorithmTask algorithmTask = new AlgorithmTask();
        algorithmTask.setLineId(lineId);
        Set<FaultWave> faultWaveSets = new HashSet<>();
        faultWaveSets.add(wave);
        algorithmTask.setFaultWaveSet(faultWaveSets);
        algorithmTask.setCreateTime(Instant.now());
        return algorithmTask;
    }

}
