package com.hy.biz.dataResolver.dto;

import com.hy.domain.DeviceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceInfoMessage extends BaseMessage {

    /**
     * 监测终端名称
     */
    private String monitoringTerminalName;

    /**
     * 监测终端型号
     */
    private String monitoringTerminalModel;

    /**
     * 监测终端基本信息版本号
     */
    private String monitoringTerminalInfoVersion;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 生产日期
     */
    private String productionDate;

    /**
     * 出厂编号
     */
    private String factoryNumber;

    /**
     * 备用
     */
    private String reserved;

    public DeviceInfo transform(long deviceId, long timeStamp) {

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceId(deviceId);
        deviceInfo.setTerminalName(this.monitoringTerminalName);
        deviceInfo.setTerminalType(this.monitoringTerminalModel);
        deviceInfo.setTerminalEdition(this.monitoringTerminalInfoVersion);
        deviceInfo.setProducer(this.manufacturer);
        deviceInfo.setProductionDate(this.productionDate);
        deviceInfo.setProducerCode(this.factoryNumber);
        deviceInfo.setCollectionTime(Instant.ofEpochMilli(timeStamp));

        return deviceInfo;
    }


}

