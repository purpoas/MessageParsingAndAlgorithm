package com.hy.biz.MessageParsing.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceStatusMessage extends BaseMessage {

    /**
     * 数据采集上传时间
     */
    private byte[] dataCollectionUploadTime;

    /**
     * 太阳能充电电流
     */
    private short solarChargingCurrent;

    /**
     * 相线取电电流
     */
    private short phasePowerCurrent;

    /**
     * 设备工作电压
     */
    private short deviceWorkingVoltage;

    /**
     * 设备工作电流
     */
    private short deviceWorkingCurrent;

    /**
     * 电池电压
     */
    private short batteryVoltage;

    /**
     * 保留位
     */
    private byte reserved;

    /**
     * 太阳能板A路电压
     */
    private short solarPanelAVoltage;

    /**
     * 太阳能板B路电压
     */
    private short solarPanelBVoltage;

    /**
     * 太阳能板C路电压
     */
    private short solarPanelCVoltage;

    /**
     * 相线取电电压
     */
    private short phasePowerVoltage;

    /**
     * 芯片温度
     */
    private short chipTemperature;

    /**
     * 主板温度
     */
    private short mainBoardTemperature;

    /**
     * 装置信号强度
     */
    private short deviceSignalStrength;

    /**
     * GPS纬度
     */
    private float gpsLatitude;

    /**
     * GPS经度
     */
    private float gpsLongitude;

}

