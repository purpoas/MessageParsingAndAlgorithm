package com.hy.biz.dataAnalysis.dto;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 算法任务-用于进行故障波形分析
 */
public class AlgorithmTask implements Delayed {

    // 线路ID
    private String lineId;

    // 故障波形集合
    private Set<String> faultWaveSet;

    // 生成时间
    private Instant createTime;

    private long timestamp = System.currentTimeMillis();

    public AlgorithmTask(String lineId, long addTimestamp) {
        this.lineId = lineId;
        this.timestamp = (this.timestamp + addTimestamp);
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public Set<String> getFaultWaveSet() {
        return faultWaveSet;
    }

    public void setFaultWaveSet(Set<String> faultWaveSet) {
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

    public synchronized static AlgorithmTask createDefaultObject(String lineId, String wave, int millisecond) {
        AlgorithmTask algorithmTask = new AlgorithmTask(lineId, millisecond);
        algorithmTask.setLineId(lineId);
        Set<String> faultWaveSets = new HashSet<>();
        faultWaveSets.add(wave);
        algorithmTask.setFaultWaveSet(faultWaveSets);
        algorithmTask.setCreateTime(Instant.now());
        return algorithmTask;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.timestamp - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
            return 1;
        } else if (this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
            return -1;
        } else {
            return 0;
        }
    }
}
