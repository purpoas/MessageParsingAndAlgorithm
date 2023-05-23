package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 算法任务唯一标识
 */
@Data
public class AlgorithmIdentify implements Delayed {

    // 主线线路ID
    private String lineId;

    // 故障波形起始时间，单位毫秒时间戳
    private Long headTime;

    // 标识同一算法任务时间范围，单位毫秒
    private Integer delayTime;

    // 算法任务唯一标识创建时间，用于比较延迟任务执行时间
    private Long createTime = System.currentTimeMillis();

    public AlgorithmIdentify(String lineId, Long headTime, Integer delayTime) {
        this.lineId = lineId;
        this.headTime = headTime;
        this.delayTime = delayTime;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public Long getHeadTime() {
        return headTime;
    }

    public void setHeadTime(Long headTime) {
        this.headTime = headTime;
    }

    public Integer getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Integer delayTime) {
        this.delayTime = delayTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlgorithmIdentify that = (AlgorithmIdentify) o;
        return lineId.equals(that.lineId) &&
                (headTime <= that.headTime + delayTime && headTime >= that.headTime);
    }

    @Override
    public int hashCode() {
        return 11;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.createTime + this.delayTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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

    @Override
    public String toString() {
        return "AlgorithmIdentify{" +
                "lineId='" + lineId + '\'' +
                ", headTime=" + headTime +
                '}';
    }
}
