package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

@Data
public class FaultWave {

    private Long deviceId;
    private String deviceCode;
    private Integer phase;             //相位

    private String lineId;             // 线路Id(主线或支线)
    private String mainLineId;         // 主线Id
    private Integer lineDepth;         // 线路与主线代差
    private String poleId;             // 杆塔Id
    private Integer poleSerial;        // 杆塔序列号
    private Double distanceToHeadStation;   //距离起始变电站距离

    private byte waveType;    // 波形类型 0x01 || 0x03 || 0x05

    private Boolean absolute;   //极性
    private String waveCode;    //波形编号
    private String headTime;    //波形起始时间
    private String data;        //波形内容

    public FaultWave() {
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
