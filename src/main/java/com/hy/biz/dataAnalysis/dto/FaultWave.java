package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

@Data
public class FaultWave {

    private Long deviceId;
    private String deviceCode;
    private String lineId;             // 线路Id(主线或支线)
    private String topMainLineId;      // 最上层主线Id
    private String poleId;             // 杆塔Id
    private Integer poleSerial;        // 杆塔序列号
    private Integer phase;
    private Double distanceToHeadStation;

    private byte waveType;    // 波形类型 0x01 || 0x03 || 0x05

    // 判断故障波形所属线路是否是主线
    // 获取设备信息 // 判断是否是支线 返回主线id

    private Boolean absolute;   //极性
    private Integer relaFlag;   //故障性质
    private String headTime;    //波形起始时间
    private String data;        //波形内容

    public FaultWave(String lineId, String headTime, String data) {
        this.lineId = lineId;
        this.headTime = headTime;
        this.data = data;
    }

    @Override
    public String toString() {
        return "FaultWave{" +
                "lineId='" + lineId + '\'' +
                ", headTime='" + headTime + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
