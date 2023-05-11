package com.hy.biz.dataResolver.entity;

import com.hy.domain.DeviceFault;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceFaultMessage extends BaseMessage {

    /**
     * 故障数据采集时间
     */
    private Instant faultDataCollectionTime;

    /**
     * 装置故障信息
     */
    private String deviceFaultInfo;

    public DeviceFault transform(long deviceId) {
        DeviceFault deviceFault = new DeviceFault();
        deviceFault.setDeviceId(deviceId);
        deviceFault.setCollectionTime(this.getFaultDataCollectionTime());
        deviceFault.setFaultDescribe(this.getDeviceFaultInfo());

        return deviceFault;
    }

}

