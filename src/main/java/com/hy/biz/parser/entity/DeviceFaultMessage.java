package com.hy.biz.parser.entity;

import com.hy.domain.DeviceFault;
import com.hy.repository.DeviceRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.hy.biz.parser.util.DateTimeUtil.parseDateTimeToInst;
import static com.hy.biz.parser.util.TypeConverter.byteArrToStr;

@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceFaultMessage extends BaseMessage {

    /**
     * 故障数据采集时间
     */
    private byte[] faultDataCollectionTime;

    /**
     * 装置故障信息
     */
    private byte[] deviceFaultInfo;

    public DeviceFault transform(DeviceRepository deviceRepository, String deviceCode) {
        DeviceFault deviceFault = new DeviceFault();
        deviceFault.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        deviceFault.setCollectionTime(parseDateTimeToInst(this.getFaultDataCollectionTime()));
        deviceFault.setFaultDescribe(byteArrToStr(this.getDeviceFaultInfo()));

        return deviceFault;
    }

}

