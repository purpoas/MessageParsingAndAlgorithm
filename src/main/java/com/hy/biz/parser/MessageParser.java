package com.hy.biz.parser;

import com.google.gson.JsonObject;
import com.hy.biz.parser.entity.*;
import com.hy.biz.parser.exception.MessageParsingException;
import com.hy.domain.*;
import com.hy.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;

import static com.hy.biz.parser.constants.FrameType.CONTROL_ACK_REPORT;
import static com.hy.biz.parser.constants.MessageConstants.*;
import static com.hy.biz.parser.constants.MessageType.PARAMETER_READING;
import static com.hy.biz.parser.util.DateTimeUtil.parseDateTimeToInst;
import static com.hy.biz.parser.util.DateTimeUtil.parseDateToStr;
import static com.hy.biz.parser.util.TypeConverter.byteArrToStr;
import static com.hy.biz.parser.util.TypeConverter.hexStringToByteArray;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;


/**
 * MessageParser 负责解析设备上传的数据，并执行相应的入库、响应等操作
 */
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
     * @return 被持久化的报文实体类
     * @description 暴露在外的解析通用方法，负责被调用
     */
    public boolean parse(long timeStamp, String commandData, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(hexStringToByteArray(commandData)).order(BIG_ENDIAN);
        boolean flag = false;

        while (buffer.hasRemaining()) {
            if (buffer.getShort() != HEADER) {
                throw new IllegalArgumentException(ILLEGAL_HEADER_ERROR);
            }

            buffer.get(new byte[ID_LENGTH]);
            byte frameType = buffer.get();
            byte messageType = buffer.get();

            String key = frameType + ":" + messageType;
            byte[] messageContent = new byte[buffer.getShort()];
            buffer.get(messageContent);
            if (MESSAGE_MAP.containsKey(key)) {
                BaseMessage specificMessage = createSpecificMsg(key, deviceCode, frameType, messageType);

                flag = parseMessageContent(messageContent, specificMessage, timeStamp, deviceCode);

                buffer.getShort(); // Skip checksum

                if (!(specificMessage instanceof WaveDataMessage) && buffer.hasRemaining()) {
                    throw new MessageParsingException(UNPARSED_DATA_ERROR);
                }
            } else if (frameType == CONTROL_ACK_REPORT && messageType == PARAMETER_READING) {
                flag = parseParamReadingMsg(messageContent, timeStamp, deviceCode);
            }

        }

        return flag;
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
    private boolean parseMessageContent(byte[] messageContent, BaseMessage specificMessage, long timeStamp, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);

        switch (specificMessage.getClass().getSimpleName()) {
            case "HeartBeatMessage":
                log.info("收到心跳数据");
                return true;
            case "BasicInfoMessage":
                return handleBasicInfoMessage((BasicInfoMessage) specificMessage, buffer, timeStamp, deviceCode);
            case "WorkingConditionMessage":
                return handleWorkingConditionMessage((WorkingConditionMessage) specificMessage, buffer, timeStamp, deviceCode);
            case "DeviceFaultMessage":
                return handleDeviceFaultMessage((DeviceFaultMessage) specificMessage, buffer, deviceCode);
            case "DeviceStatusMessage":
                return handleDeviceStatusMessage((DeviceStatusMessage) specificMessage, buffer, deviceCode);
            case "WaveDataMessage":
                return handleWaveData((WaveDataMessage) specificMessage, buffer, timeStamp, deviceCode);
            default:
                throw new MessageParsingException(ILLEGAL_MESSAGE_SIGNATURE_ERROR);
        }
    }

    private boolean handleBasicInfoMessage(BasicInfoMessage message, ByteBuffer buffer, long timeStamp, String deviceCode) {

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

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        deviceInfo.setTerminalName(byteArrToStr(message.getMonitoringTerminalName()));
        deviceInfo.setTerminalType(byteArrToStr(message.getMonitoringTerminalModel()));
        deviceInfo.setTerminalEdition(
                new BigDecimal(String.valueOf(message.getMonitoringTerminalInfoVersion()))
                        .setScale(4, RoundingMode.HALF_UP)
                        .toString());
        deviceInfo.setProducer(byteArrToStr(message.getManufacturer()));
        deviceInfo.setProducerCode(Long.toString(ByteBuffer.wrap(message.getFactoryNumber()).order(LITTLE_ENDIAN).getLong()));
        deviceInfo.setProductionDate(parseDateToStr(message.getProductionDate()));
        deviceInfo.setCollectionTime(Instant.ofEpochMilli(timeStamp));


        deviceInfoRepository.save(deviceInfo);
        return true;
    }

    private boolean handleWorkingConditionMessage(WorkingConditionMessage message, ByteBuffer buffer, long timeStamp, String deviceCode) {

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

        WorkStatus workStatus = new WorkStatus();
        workStatus.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        workStatus.setCollectionTime(Instant.ofEpochMilli(timeStamp));
        workStatus.setDeviceTemperature((float) message.getDeviceTemperature());
        workStatus.setLineCurrent((float) message.getCurrentEffectiveValue());

        workStatusRepository.save(workStatus);
        return true;
    }

    private boolean handleDeviceFaultMessage(DeviceFaultMessage message, ByteBuffer buffer, String deviceCode) {
        byte[] time = new byte[TIME_LENGTH];
        buffer.get(time);
        message.setFaultDataCollectionTime(time);
        int remaining = buffer.remaining();
        byte[] info = new byte[remaining];
        buffer.get(info);
        message.setDeviceFaultInfo(info);

        DeviceFault deviceFault = new DeviceFault();
        deviceFault.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        deviceFault.setCollectionTime(parseDateTimeToInst(message.getFaultDataCollectionTime()));
        deviceFault.setFaultDescribe(byteArrToStr(message.getDeviceFaultInfo()));

        deviceFaultRepository.save(deviceFault);
        return true;
    }

    private boolean handleDeviceStatusMessage(DeviceStatusMessage message, ByteBuffer buffer, String deviceCode) {
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

        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setCollectionTime(parseDateTimeToInst(message.getDataCollectionUploadTime()));
        deviceStatus.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        deviceStatus.setSolarChargeCurrent((int) message.getSolarChargingCurrent());
        deviceStatus.setPhasePowerCurrent((int) message.getPhasePowerCurrent());
        deviceStatus.setWorkVoltage((int) message.getDeviceWorkingVoltage());
        deviceStatus.setWorkCurrent((int) message.getDeviceWorkingCurrent());
        deviceStatus.setBatteryVoltage((int) message.getBatteryVoltage());
        deviceStatus.setReserved((int) message.getReserved());
        deviceStatus.setSolarPanelAVoltage((int) message.getSolarPanelAVoltage());
        deviceStatus.setSolarPanelBVoltage((int) message.getSolarPanelBVoltage());
        deviceStatus.setSolarPanelCVoltage((int) message.getSolarPanelCVoltage());
        deviceStatus.setPhasePowerVoltage((int) message.getPhasePowerVoltage());
        deviceStatus.setChipTemperature((int) message.getChipTemperature());
        deviceStatus.setMainboardTemperature((int) message.getMainBoardTemperature());
        deviceStatus.setSignalStrength((int) message.getDeviceSignalStrength());
        deviceStatus.setGpsLatitude((int) message.getGpsLatitude());
        deviceStatus.setGpsLongitude((int) message.getGpsLongitude());

        deviceStatusRepository.save(deviceStatus);
        return true;
    }

    private boolean handleWaveData(WaveDataMessage message, ByteBuffer buffer, long timeStamp, String deviceCode) {
        message.setDataPacketLength(buffer.getShort());
        byte[] wave = new byte[message.getDataPacketLength()];
        log.info("这段报文的波形数据长度： {}", message.getDataPacketLength());
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

        byte segmentNumber = message.getSegmentNumber();
        byte dataPacketNumber = message.getDataPacketNumber();

        if (segmentNumber == 1) {
            waveData = new WaveData();
            waveDataParserHelper.setWaveDataProperties(waveData, message, timeStamp, deviceCode);
        } else if (segmentNumber <= dataPacketNumber) {
            waveDataParserHelper.appendWaveData(waveData, message);
        }

        waveDataRepository.save(waveData);
        return true;
    }

    private boolean parseParamReadingMsg(byte[] messageContent, long timeStamp, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);
        byte stat = buffer.get();
        String status;

        switch (stat) {
            case MESSAGE_STATUS_SUCCESS:
                status = "success";
                break;
            case MESSAGE_STATUS_FAILURE:
                status = "failure";
                break;
            default:
                throw new MessageParsingException("Invalid message status: " + stat);
        }

        JsonObject paramReadingMsg = new JsonObject();
        paramReadingMsg.addProperty("status", status);
        paramReadingMsg.addProperty("timeStamp", Instant.ofEpochMilli(timeStamp).toString());
        paramReadingMsg.addProperty("deviceId", Long.toString(deviceRepository.findDeviceIdByCode(deviceCode)));

        short paramNum = buffer.getShort();
        JsonObject params = new JsonObject();

        for (int i = 0; i < paramNum; i++) {
            short paramId = buffer.getShort();
            int paramContent = buffer.getInt();

            String paramIdStr = String.format("param%d", paramId);
            String paramContentStr = Integer.toString(paramContent);

            params.addProperty(paramIdStr, paramContentStr);
        }

        paramReadingMsg.add("param", params);

        return true;
    }

}


