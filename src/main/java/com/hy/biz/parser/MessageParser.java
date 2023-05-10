package com.hy.biz.parser;

import com.hy.biz.parser.entity.*;
import com.hy.biz.parser.exception.MessageParsingException;
import com.hy.biz.parser.registry.MessageClassRegistry;
import com.hy.biz.parser.util.WaveDataParserHelper;
import com.hy.domain.*;
import com.hy.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.hy.biz.parser.constants.MessageConstants.*;
import static com.hy.biz.parser.util.TypeConverter.hexStringToByteArray;
import static java.nio.ByteOrder.BIG_ENDIAN;


/**
 * @author shiwentao
 * @package com.hy.biz.parser
 * @description 解析器：用于解析 Redis 阻塞队列中的报文数据
 * @create 2023-05-08 16:12
 **/
@Component
@Transactional
@Slf4j
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MessageParser {
    private final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = MessageClassRegistry.getMessageMap();
    private final WaveDataParserHelper waveDataParserHelper;
    private WaveData waveData;

    private final WaveDataRepository waveDataRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final WorkStatusRepository workStatusRepository;
    private final DeviceFaultRepository deviceFaultRepository;
    private final DeviceStatusRepository deviceStatusRepository;
    private final DeviceRepository deviceRepository;

    public MessageParser(WaveDataParserHelper waveDataParserHelper, WaveDataRepository waveDataRepository, DeviceInfoRepository deviceInfoRepository, WorkStatusRepository workStatusRepository, DeviceFaultRepository deviceFaultRepository, DeviceStatusRepository deviceStatusRepository, DeviceRepository deviceRepository) {
        this.waveDataParserHelper = waveDataParserHelper;
        this.waveDataRepository = waveDataRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.workStatusRepository = workStatusRepository;
        this.deviceFaultRepository = deviceFaultRepository;
        this.deviceStatusRepository = deviceStatusRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * @param timeStamp   报文被接收到的时间戳
     * @param commandData 十六进制的报文内容
     * @param deviceCode  设备编号
     * @return            是否解析成功
     * @description       暴露在外的解析方法，用于被调用
     */
    public Object parse(long timeStamp, String commandData, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(hexStringToByteArray(commandData)).order(BIG_ENDIAN);
        Object entitySaved = null;

        while (buffer.hasRemaining()) {
            if (buffer.getShort() != HEADER) throw new IllegalArgumentException(ILLEGAL_HEADER_ERROR);
            buffer.position(buffer.position() + ID_LENGTH);
            byte frameType = buffer.get();
            byte messageType = buffer.get();
            String key = frameType + ":" + messageType;
            byte[] messageContent = new byte[buffer.getShort()];
            buffer.get(messageContent);
            if (MESSAGE_MAP.containsKey(key)) {
                BaseMessage specificMessage = createSpecificMsg(key, deviceCode, frameType, messageType);
                entitySaved = parseMessageContent(key, messageContent, specificMessage, timeStamp, deviceCode);
                buffer.position(buffer.position() + CHECK_SUM_LENGTH); // Skip checksum
                if (!(specificMessage instanceof WaveDataMessage) && buffer.hasRemaining())
                    throw new MessageParsingException(UNPARSED_DATA_ERROR);
            }
        }

        return entitySaved;
    }

    /**
     * @param key         报文签名
     * @param deviceCode  设备编码
     * @param messageType 报文类型
     * @return 具体报文实体类
     * @description 该方法通过报文签名key，在 MESSAGE_MAP 中找到对应的报文实体类，并进行创建初始化
     */
    private BaseMessage createSpecificMsg(String key, String deviceCode, byte frameType, byte messageType) {
        Class<? extends BaseMessage> messageClass = MESSAGE_MAP.get(key);
        BaseMessage specificMessage;
        try {
            specificMessage = messageClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MessageParsingException(UNSUPPORTED_DATA_TYPE_ERROR, e);
        }

        specificMessage.setIdNumber(deviceCode.getBytes());
        specificMessage.setFrameType(frameType);
        specificMessage.setMessageType(messageType);

        return specificMessage;
    }

    /**
     * @param messageContent  报文内容
     * @param specificMessage 具体报文类型
     * @param timeStamp       时间戳
     * @param deviceCode      设备编码
     * @return 被持久化的报文实体类
     * @description 该方法根据收到的报文类型，调用负责解析该类型的解析方法
     */
    private Object parseMessageContent(String key, byte[] messageContent, BaseMessage specificMessage, long timeStamp, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);
        String messageSignature = String.format("0x%02X:0x%02X", Integer.parseInt(key.split(":")[0]), Integer.parseInt(key.split(":")[1]));

        switch (messageSignature) {
            case "0x05:0x01":
                log.info("收到心跳数据");
            case "0x05:0x03":
                return parseBasicInfoMessage((BasicInfoMessage) specificMessage, buffer, timeStamp, deviceCode);
            case "0x05:0x05":
                return parseWorkingConditionMessage((WorkingConditionMessage) specificMessage, buffer, timeStamp, deviceCode);
            case "0x05:0x07":
                return parseDeviceFaultMessage((DeviceFaultMessage) specificMessage, buffer, deviceCode);
            case "0x05:0x0A":
                return parseDeviceStatusMessage((DeviceStatusMessage) specificMessage, buffer, deviceCode);
            case "0x01:0x01":
            case "0x01:0x03":
            case "0x01:0x05":
                return parseWaveData((WaveDataMessage) specificMessage, buffer, timeStamp, deviceCode);
            default:
                throw new MessageParsingException(ILLEGAL_MESSAGE_SIGNATURE_ERROR);
        }

    }

    private Object parseBasicInfoMessage(BasicInfoMessage message, ByteBuffer buffer, long timeStamp, String deviceCode) {

        byte[] terminalName = new byte[MONITORING_TERMINAL_NAME_LENGTH];
        buffer.get(terminalName);
        message.setMonitoringTerminalName(terminalName);
        byte[] model = new byte[MONITORING_TERMINAL_MODEL_LENGTH];
        buffer.get(model);
        message.setMonitoringTerminalModel(model);
        message.setMonitoringTerminalInfoVersion(buffer.getFloat());
        byte[] manufacturer = new byte[MANUFACTURER_LENGTH];
        buffer.get(manufacturer);
        message.setManufacturer(manufacturer);
        byte[] date = new byte[SIMPLE_DATE_LENGTH];
        buffer.get(date);
        message.setProductionDate(date);
        byte[] serialNumber = new byte[FACTORY_SERIAL_NUMBER_LENGTH];
        buffer.get(serialNumber);
        message.setFactoryNumber(serialNumber);
        byte[] reserved = new byte[RESERVED_LENGTH];
        buffer.get(reserved);
        message.setReserved(reserved);

        DeviceInfo deviceInfo = message.transform(deviceRepository, timeStamp, deviceCode);

        return deviceInfoRepository.save(deviceInfo);
    }

    private Object parseWorkingConditionMessage(WorkingConditionMessage message, ByteBuffer buffer, long timeStamp, String deviceCode) {

        byte[] time = new byte[TIME_LENGTH];
        buffer.get(time);
        message.setUploadTime(time);
        message.setBatteryPowerState(buffer.get());
        message.setBatteryVoltage(buffer.getShort());
        message.setDeviceTemperature(buffer.getShort());
        message.setCurrentEffectiveValue(buffer.getShort());
        byte[] reserved = new byte[RESERVED_LENGTH];
        buffer.get(reserved);
        message.setReserved(reserved);

        WorkStatus workStatus = message.transform(deviceRepository, timeStamp, deviceCode);

        return workStatusRepository.save(workStatus);
    }

    private Object parseDeviceFaultMessage(DeviceFaultMessage message, ByteBuffer buffer, String deviceCode) {
        byte[] time = new byte[TIME_LENGTH];
        buffer.get(time);
        message.setFaultDataCollectionTime(time);
        int remaining = buffer.remaining();
        byte[] info = new byte[remaining];
        buffer.get(info);
        message.setDeviceFaultInfo(info);

        DeviceFault deviceFault = message.transform(deviceRepository, deviceCode);

        return deviceFaultRepository.save(deviceFault);
    }

    private Object parseDeviceStatusMessage(DeviceStatusMessage message, ByteBuffer buffer, String deviceCode) {
        byte[] time = new byte[TIME_LENGTH];
        buffer.get(time);
        message.setDataCollectionUploadTime(time);
        message.setSolarChargingCurrent(buffer.getShort());
        message.setPhasePowerCurrent(buffer.getShort());
        message.setDeviceWorkingVoltage(buffer.getShort());
        message.setDeviceWorkingCurrent(buffer.getShort());
        message.setBatteryVoltage(buffer.getShort());
        message.setReserved(buffer.get());
        message.setSolarPanelAVoltage(buffer.getShort());
        message.setSolarPanelBVoltage(buffer.getShort());
        message.setSolarPanelCVoltage(buffer.getShort());
        message.setPhasePowerVoltage(buffer.getShort());
        message.setChipTemperature(buffer.getShort());
        message.setMainBoardTemperature(buffer.getShort());
        message.setDeviceSignalStrength(buffer.getShort());
        message.setGpsLatitude(buffer.getFloat());
        message.setGpsLongitude(buffer.getFloat());

        DeviceStatus deviceStatus = message.transform(deviceRepository, deviceCode);

        return deviceStatusRepository.save(deviceStatus);
    }

    private Object parseWaveData(WaveDataMessage message, ByteBuffer buffer, long timeStamp, String deviceCode) {
        message.setDataPacketLength(buffer.getShort());
        log.info("这段报文的波形数据长度： {}", message.getDataPacketLength());
        byte[] wave = new byte[message.getDataPacketLength()];
        buffer.get(wave);
        message.setWaveData(wave);
        byte[] waveStartTime = new byte[TIME_LENGTH];
        buffer.get(waveStartTime);
        message.setWaveStartTime(waveStartTime);
        message.setWaveDataLength(buffer.getShort());
        log.info("波形数据总长度： {}", message.getWaveDataLength());
        message.setSegmentNumber(buffer.get());
        log.info("当前报文段号： {}", message.getSegmentNumber());
        message.setDataPacketNumber(buffer.get());
        log.info("总报文段数： {}", message.getDataPacketNumber());
        int remaining = buffer.remaining();
        byte[] reserved = new byte[remaining];
        buffer.get(reserved);
        message.setReserved(reserved);

        waveData = message.transform(this.waveData, waveDataParserHelper, timeStamp, deviceCode);

        return waveDataRepository.save(waveData);
    }

}


