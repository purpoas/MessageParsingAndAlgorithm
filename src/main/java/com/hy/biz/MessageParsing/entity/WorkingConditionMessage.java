package com.hy.biz.MessageParsing.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class WorkingConditionMessage extends BaseMessage {

    /**
     * 工况上传时间
     */
    private byte[] uploadTime;

    /**
     * 电池供电状态
     */
    private byte batteryPowerState;

    /**
     * 电池电压
     */
    private short batteryVoltage;

    /**
     * 设备温度
     */
    private short deviceTemperature;

    /**
     * 电流有效值
     */
    private short currentEffectiveValue;

    /**
     * 备用
     */
    private byte[] reserved;
}

