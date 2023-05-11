package com.hy.biz.dataResolver.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.dataResolver.constants.MessageConstants;
import com.hy.biz.dataResolver.entity.*;
import com.hy.biz.dataResolver.entity.dto.MessageDTO;
import com.hy.biz.dataResolver.exception.MessageParsingException;
import com.hy.biz.dataResolver.registry.MessageClassRegistry;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Map;

import static com.hy.biz.dataResolver.util.DateTimeUtil.parseDateTimeToInst;
import static com.hy.biz.dataResolver.util.DateTimeUtil.parseDateToStr;
import static com.hy.biz.dataResolver.util.TypeConverter.byteArrToStr;
import static com.hy.biz.dataResolver.util.TypeConverter.hexStringToByteArray;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;


/**
 *
 * 解析器：用于解析 Redis 阻塞队列中的报文数据
 *
 * @author shiwentao
 * @package com.hy.biz.dataResolver
 * @create 2023-05-08 16:12
 **/
@Component
@Transactional
@Slf4j
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MessageParser {

    private final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = MessageClassRegistry.getMessageMap();

    /**
     * @param data        json字符串
     * @return            具体报文类型
     */
    public BaseMessage parse(String data) {
        MessageDTO messageDTO;
        try {
            messageDTO = new ObjectMapper().readValue(data, MessageDTO.class);
        } catch (JsonProcessingException e) {
            throw new MessageParsingException("接收到的 JSON 字符串无法转换为MessageDTO对象", e);
        }

        String commandData = messageDTO.getData().getCommand();

        ByteBuffer buffer = ByteBuffer.wrap(hexStringToByteArray(commandData)).order(BIG_ENDIAN);
        BaseMessage messageParsed = null;

        while (buffer.hasRemaining()) {
            if (buffer.getShort() != MessageConstants.HEADER) throw new IllegalArgumentException(MessageConstants.ILLEGAL_HEADER_ERROR);
            buffer.position(buffer.position() + MessageConstants.ID_LENGTH);
            byte frameType = buffer.get();
            byte messageType = buffer.get();
            String key = frameType + ":" + messageType;
            byte[] messageContent = new byte[buffer.getShort()];
            buffer.get(messageContent);
            if (MESSAGE_MAP.containsKey(key)) {
                BaseMessage specificMessage = createSpecificMsg(key, frameType, messageType);
                messageParsed = parseMessageContent(key, messageContent, specificMessage);
                buffer.position(buffer.position() + MessageConstants.CHECK_SUM_LENGTH); // Skip checksum
                if (!(specificMessage instanceof WaveDataMessage) && buffer.hasRemaining())
                    throw new MessageParsingException(MessageConstants.UNPARSED_DATA_ERROR);
            }
        }

        return messageParsed;
    }

    /**
     * @param key         报文签名
     * @param messageType 报文类型
     * @return 具体报文实体类
     * @description 该方法通过报文签名key，在 MESSAGE_MAP 中找到对应的报文实体类，并进行创建初始化
     */
    private BaseMessage createSpecificMsg(String key, byte frameType, byte messageType) {
        Class<? extends BaseMessage> messageClass = MESSAGE_MAP.get(key);
        BaseMessage specificMessage;
        try {
            specificMessage = messageClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MessageParsingException(MessageConstants.UNSUPPORTED_DATA_TYPE_ERROR, e);
        }

        specificMessage.setFrameType(frameType);
        specificMessage.setMessageType(messageType);

        return specificMessage;
    }

    /**
     * @param messageContent  报文内容
     * @param specificMessage 具体报文类型
     * @return 被持久化的报文实体类
     * @description 该方法根据收到的报文类型，调用负责解析该类型的解析方法
     */
    private BaseMessage parseMessageContent(String key, byte[] messageContent, BaseMessage specificMessage) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);

        String messageSignature = String.format("0x%02X:0x%02X", Integer.parseInt(key.split(":")[0]), Integer.parseInt(key.split(":")[1]));
        switch (messageSignature) {
            case "0x05:0x01":
                log.info("收到心跳数据");
            case "0x05:0x03":
                return parseBasicInfoMessage((DeviceInfoMessage) specificMessage, buffer);
            case "0x05:0x05":
                return parseWorkStatusMessage((WorkStatusMessage) specificMessage, buffer);
            case "0x05:0x07":
                return parseDeviceFaultMessage((DeviceFaultMessage) specificMessage, buffer);
            case "0x05:0x0A":
                return parseDeviceStatusMessage((DeviceStatusMessage) specificMessage, buffer);
            case "0x01:0x01":
            case "0x01:0x03":
            case "0x01:0x05":
                return parseWaveData((WaveDataMessage) specificMessage, buffer);
            default:
                throw new MessageParsingException(MessageConstants.ILLEGAL_MESSAGE_SIGNATURE_ERROR);
        }

    }

    private BaseMessage parseBasicInfoMessage(DeviceInfoMessage message, ByteBuffer buffer) {

        byte[] terminalName = new byte[MessageConstants.MONITORING_TERMINAL_NAME_LENGTH];
        buffer.get(terminalName);
        message.setMonitoringTerminalName(byteArrToStr(terminalName));
        byte[] model = new byte[MessageConstants.MONITORING_TERMINAL_MODEL_LENGTH];
        buffer.get(model);
        message.setMonitoringTerminalModel(byteArrToStr(model));
        message.setMonitoringTerminalInfoVersion(new BigDecimal(String.valueOf(buffer.getFloat()))
                .setScale(4, RoundingMode.HALF_UP)
                .toString());
        byte[] manufacturer = new byte[MessageConstants.MANUFACTURER_LENGTH];
        buffer.get(manufacturer);
        message.setManufacturer(byteArrToStr(manufacturer));
        byte[] date = new byte[MessageConstants.SIMPLE_DATE_LENGTH];
        buffer.get(date);
        message.setProductionDate(parseDateToStr(date));
        byte[] serialNumber = new byte[MessageConstants.FACTORY_SERIAL_NUMBER_LENGTH];
        buffer.get(serialNumber);
        message.setFactoryNumber(Long.toString(ByteBuffer.wrap(serialNumber).order(LITTLE_ENDIAN).getLong()));
        byte[] reserved = new byte[MessageConstants.RESERVED_LENGTH];
        buffer.get(reserved);
        message.setReserved(byteArrToStr(reserved));

        return message;
    }

    private BaseMessage parseWorkStatusMessage(WorkStatusMessage message, ByteBuffer buffer) {

        buffer.position(buffer.position() + 15);
        byte[] deviceTemperature = new byte[2];
        buffer.get(deviceTemperature);
        message.setDeviceTemperature(ByteBuffer.wrap(deviceTemperature).order(BIG_ENDIAN).getFloat());
        byte[] currentEffectiveValue = new byte[2];
        buffer.get(currentEffectiveValue);
        message.setCurrentEffectiveValue(ByteBuffer.wrap(currentEffectiveValue).order(BIG_ENDIAN).getFloat());

        return message;
    }

    private BaseMessage parseDeviceFaultMessage(DeviceFaultMessage message, ByteBuffer buffer) {
        byte[] time = new byte[MessageConstants.TIME_LENGTH];
        buffer.get(time);
        message.setFaultDataCollectionTime(parseDateTimeToInst(time));
        int remaining = buffer.remaining();
        byte[] info = new byte[remaining];
        buffer.get(info);
        message.setDeviceFaultInfo(byteArrToStr(info));

        return message;
    }

    private BaseMessage parseDeviceStatusMessage(DeviceStatusMessage message, ByteBuffer buffer) {
        byte[] time = new byte[MessageConstants.TIME_LENGTH];
        buffer.get(time);
        message.setDataCollectionUploadTime(parseDateTimeToInst(time));
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

        return message;
    }

    private BaseMessage parseWaveData(WaveDataMessage message, ByteBuffer buffer) {
        message.setDataPacketLength(buffer.getShort());
        log.info("这段报文的波形数据长度： {}", message.getDataPacketLength());
        byte[] wave = new byte[message.getDataPacketLength()];
        buffer.get(wave);
        message.setWaveData(byteArrToStr(wave));
        byte[] waveStartTime = new byte[MessageConstants.TIME_LENGTH];
        buffer.get(waveStartTime);
        message.setWaveStartTime(parseDateToStr(waveStartTime));
        message.setWaveDataLength(buffer.getShort());
        log.info("波形数据总长度： {}", message.getWaveDataLength());
        message.setSegmentNumber(buffer.get());
        log.info("当前报文段号： {}", message.getSegmentNumber());
        message.setDataPacketNumber(buffer.get());
        log.info("总报文段数： {}", message.getDataPacketNumber());
        int remaining = buffer.remaining();
        byte[] reserved = new byte[remaining];
        buffer.get(reserved);
        message.setReserved(byteArrToStr(reserved));

        return message;
    }

}


