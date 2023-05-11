package com.hy.biz.dataResolver.entity;

import com.hy.domain.DeviceStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceStatusMessage extends BaseMessage {

    /**
     * 数据采集上传时间
     */
    private Instant dataCollectionUploadTime;

    /**
     * 太阳能充电电流
     */
    private int solarChargingCurrent;

    /**
     * 相线取电电流
     */
    private int phasePowerCurrent;

    /**
     * 设备工作电压
     */
    private int deviceWorkingVoltage;

    /**
     * 设备工作电流
     */
    private int deviceWorkingCurrent;

    /**
     * 电池电压
     */
    private int batteryVoltage;

    /**
     * 保留位
     */
    private int reserved;

    /**
     * 太阳能板A路电压
     */
    private int solarPanelAVoltage;

    /**
     * 太阳能板B路电压
     */
    private int solarPanelBVoltage;

    /**
     * 太阳能板C路电压
     */
    private int solarPanelCVoltage;

    /**
     * 相线取电电压
     */
    private int phasePowerVoltage;

    /**
     * 芯片温度
     */
    private int chipTemperature;

    /**
     * 主板温度
     */
    private int mainBoardTemperature;

    /**
     * 装置信号强度
     */
    private int deviceSignalStrength;

    /**
     * GPS纬度
     */
    private float gpsLatitude;

    /**
     * GPS经度
     */
    private float gpsLongitude;

    public DeviceStatus transform(long deviceId) {
        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setCollectionTime(this.dataCollectionUploadTime);
        deviceStatus.setDeviceId(deviceId);
        deviceStatus.setSolarChargeCurrent(this.solarChargingCurrent);
        deviceStatus.setPhasePowerCurrent(this.phasePowerCurrent);
        deviceStatus.setWorkVoltage(this.deviceWorkingVoltage);
        deviceStatus.setWorkCurrent(this.deviceWorkingCurrent);
        deviceStatus.setBatteryVoltage(this.batteryVoltage);
        deviceStatus.setReserved(this.reserved);
        deviceStatus.setSolarPanelAVoltage(this.solarPanelAVoltage);
        deviceStatus.setSolarPanelBVoltage(this.solarPanelBVoltage);
        deviceStatus.setSolarPanelCVoltage(this.solarPanelCVoltage);
        deviceStatus.setPhasePowerVoltage(this.phasePowerVoltage);
        deviceStatus.setChipTemperature(this.chipTemperature);
        deviceStatus.setMainboardTemperature(this.mainBoardTemperature);
        deviceStatus.setSignalStrength(this.deviceSignalStrength);
        deviceStatus.setGpsLatitude((int) this.gpsLatitude);
        deviceStatus.setGpsLongitude((int) this.gpsLongitude);

        return deviceStatus;
    }

}

