package com.hy.biz.dataAnalysis.dto;

/**
 * 算法任务唯一标识
 */
public class AlgorithmIdentify {

    // 线路ID
    private String lineId;

    // 故障波形起始时间，单位秒时间戳
    private Long headTime;

    // 标识同一算法任务时间范围，单位秒
    private Integer timeRange;


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

    public Integer getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(Integer timeRange) {
        this.timeRange = timeRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlgorithmIdentify that = (AlgorithmIdentify) o;
        return lineId.equals(that.lineId) &&
                (headTime <= that.headTime + timeRange && headTime >= that.headTime);
    }

    @Override
    public int hashCode() {
        return 11;
    }
}
