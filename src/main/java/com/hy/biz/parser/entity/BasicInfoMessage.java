package com.hy.biz.parser.entity;

import com.hy.domain.DeviceInfo;
import com.hy.repository.DeviceRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.time.Instant;

import static com.hy.biz.parser.util.DateTimeUtil.parseDateToStr;
import static com.hy.biz.parser.util.TypeConverter.byteArrToStr;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

@Data
@EqualsAndHashCode(callSuper = false)
public class BasicInfoMessage extends BaseMessage {

    /**
     * 监测终端名称
     */
    private byte[] monitoringTerminalName;

    /**
     * 监测终端型号
     */
    private byte[] monitoringTerminalModel;

    /**
     * 监测终端基本信息版本号
     */
    private float monitoringTerminalInfoVersion;

    /**
     * 生产厂家
     */
    private byte[] manufacturer;

    /**
     * 生产日期
     */
    private byte[] productionDate;

    /**
     * 出厂编号
     */
    private byte[] factoryNumber;

    /**
     * 备用
     */
    private byte[] reserved;

    public DeviceInfo transform(DeviceRepository deviceRepository, long timeStamp, String deviceCode) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        deviceInfo.setTerminalName(byteArrToStr(this.getMonitoringTerminalName()));
        deviceInfo.setTerminalType(byteArrToStr(this.getMonitoringTerminalModel()));
        deviceInfo.setTerminalEdition(
                new BigDecimal(String.valueOf(this.getMonitoringTerminalInfoVersion()))
                        .setScale(4, RoundingMode.HALF_UP)
                        .toString());
        deviceInfo.setProducer(byteArrToStr(this.getManufacturer()));
        deviceInfo.setProducerCode(Long.toString(ByteBuffer.wrap(this.getFactoryNumber()).order(LITTLE_ENDIAN).getLong()));
        deviceInfo.setProductionDate(parseDateToStr(this.getProductionDate()));
        deviceInfo.setCollectionTime(Instant.ofEpochMilli(timeStamp));
        return deviceInfo;
    }

}

