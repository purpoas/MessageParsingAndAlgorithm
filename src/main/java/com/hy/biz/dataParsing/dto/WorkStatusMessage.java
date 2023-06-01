package com.hy.biz.dataParsing.dto;

import com.hy.domain.WorkStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
public class WorkStatusMessage extends BaseMessage {

    /**
     * 工况上传时间 长度：12字节
     */
    private Instant uploadTime;

    /**
     * 电池供电状态 长度：1字节
     */
    private int batteryPowerState;

    /**
     * 电池电压 长度：2字节
     */
    private int batteryVoltage;

    /**
     * 设备温度 长度：2字节
     */
    private float deviceTemperature;

    /**
     * 电流有效值 长度：2字节
     */
    private float currentEffectiveValue;

    /**
     * 备用 长度：30字节
     */
    private String reserved;


    public WorkStatus transform(long deviceId) {

        WorkStatus workStatus = new WorkStatus();
        workStatus.setDeviceId(deviceId);
        workStatus.setCollectionTime(this.uploadTime);
        workStatus.setDeviceTemperature(this.deviceTemperature);
        workStatus.setLineCurrent(this.currentEffectiveValue);

        return workStatus;
    }


}

