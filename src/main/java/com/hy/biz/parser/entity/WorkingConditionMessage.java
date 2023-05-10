package com.hy.biz.parser.entity;

import com.hy.domain.WorkStatus;
import com.hy.repository.DeviceRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

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

    public WorkStatus transform(DeviceRepository deviceRepository, long timeStamp, String deviceCode) {
        WorkStatus workStatus = new WorkStatus();
        workStatus.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        workStatus.setCollectionTime(Instant.ofEpochMilli(timeStamp));
        workStatus.setDeviceTemperature((float) this.getDeviceTemperature());
        workStatus.setLineCurrent((float) this.getCurrentEffectiveValue());

        return workStatus;
    }
}

