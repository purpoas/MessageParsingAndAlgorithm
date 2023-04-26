package com.hy.biz.parser;

import com.hy.biz.parser.entity.*;
import com.hy.biz.parser.exception.MessageParserException;
import com.hy.config.HyConfigProperty;
import com.hy.domain.*;
import com.hy.repository.*;
import com.hy.web.HeartBeatController;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import static com.hy.biz.parser.constants.MessageConstants.*;
import static com.hy.biz.parser.util.DataTypeConverter.*;
import static com.hy.biz.parser.util.DateTimeUtil.parseDateTimeToInst;
import static com.hy.biz.parser.util.DateTimeUtil.parseDateTimeToStr;
import static io.netty.util.internal.StringUtil.byteToHexString;


/**
 * MessageParser 负责解析设备上传的数据，并执行相应的入库、响应等操作
 */
@Component
@Transactional
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MessageParser {
    private final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    private final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = MessageClassRegistry.getMessageMap();
    private final HyConfigProperty hyConfigProperty;
    private final WaveDataRepository waveDataRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final WorkStatusRepository workStatusRepository;
    private final DeviceFaultRepository deviceFaultRepository;
    private final DeviceStatusRepository deviceStatusRepository;
    private final DeviceRepository deviceRepository;
    private final HeartBeatController heartBeatController;
    private WaveData waveData;

    public MessageParser(HyConfigProperty hyConfigProperty, WaveDataRepository waveDataRepository, DeviceInfoRepository deviceInfoRepository, WorkStatusRepository workStatusRepository, DeviceFaultRepository deviceFaultRepository, DeviceStatusRepository deviceStatusRepository, DeviceRepository deviceRepository, HeartBeatController heartBeatController) {
        this.hyConfigProperty = hyConfigProperty;
        this.waveDataRepository = waveDataRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.workStatusRepository = workStatusRepository;
        this.deviceFaultRepository = deviceFaultRepository;
        this.deviceStatusRepository = deviceStatusRepository;
        this.deviceRepository = deviceRepository;
        this.heartBeatController = heartBeatController;
    }

    /**
     * @description       Parses the command data from the device.
     * @param dateTime    the date and time when the message was received.
     * @param commandData the command data in hexadecimal format.
     * @param deviceCode  the device code.
     */
    public void parse(byte[] dateTime, String commandData, String deviceCode) {
        processMessage(commandData, dateTime, deviceCode);
    }

    private void processMessage(String hexString, byte[] dateTime, String deviceCode) {
        byte[] command = hexStringToByteArray(hexString);
        ByteBuffer buffer = ByteBuffer.wrap(command).order(BYTE_ORDER);

        while (buffer.hasRemaining()) {
            if (buffer.getShort() != HEADER) {
                throw new IllegalArgumentException(ILLEGAL_HEADER_ERROR);
            }

            buffer.get(new byte[ID_LENGTH]);
            byte frameType = buffer.get();
            byte messageType = buffer.get();
            String key = frameType + ":" + messageType;
            BaseMessage specificMessage = createSpecificMessage(key, deviceCode, messageType);

            short messageLength = buffer.getShort();
            byte[] messageContent = new byte[messageLength];
            buffer.get(messageContent);
            parseMessageContent(messageContent, specificMessage, dateTime, deviceCode);
            buffer.getShort(); // Skip checksum

            if (!(specificMessage instanceof WaveDataMessage) && buffer.hasRemaining()) {
                throw new MessageParserException(UNPARSED_DATA_ERROR);
            }
        }

    }

    private BaseMessage createSpecificMessage(String key, String deviceCode, byte messageType) {
        Class<? extends BaseMessage> messageClass = MESSAGE_MAP.get(key);
        BaseMessage specificMessage;
        try {
            specificMessage = messageClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MessageParserException(UNSUPPORTED_DATA_TYPE_ERROR, e);
        }
        specificMessage.setIdNumber(deviceCode.getBytes());
        specificMessage.setMessageType(messageType);
        return specificMessage;
    }

    private void parseMessageContent(byte[] messageContent, BaseMessage specificMessage, byte[] dateTime, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BYTE_ORDER);

        if (specificMessage instanceof HeartBeatMessage) {
            //todo
            heartBeatController.heartBeat();
        } else if (specificMessage instanceof BasicInfoMessage) {
            handleBasicInfoMessage((BasicInfoMessage) specificMessage, buffer, dateTime, deviceCode);
        } else if (specificMessage instanceof WorkingConditionMessage) {
            handleWorkingConditionMessage((WorkingConditionMessage) specificMessage, buffer, deviceCode);
        } else if (specificMessage instanceof DeviceFaultMessage) {
            handleDeviceFaultMessage((DeviceFaultMessage) specificMessage, buffer, deviceCode);
        } else if (specificMessage instanceof DeviceStatusMessage) {
            handleDeviceStatusMessage((DeviceStatusMessage) specificMessage, buffer, deviceCode);
        } else if (specificMessage instanceof WaveDataMessage) {
            handleWaveData((WaveDataMessage) specificMessage, buffer, dateTime, deviceCode);
        } else {
            throw new MessageParserException(ILLEGAL_MESSAGE_SIGNATURE_ERROR);
        }
    }

    private void handleBasicInfoMessage(BasicInfoMessage message, ByteBuffer buffer, byte[] dateTime, String deviceCode) {

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
        message.setProductionDate(buffer.getFloat());
        byte[] serialNumber = new byte[FACTORY_SERIAL_NUMBER_LENGTH];
        buffer.get(serialNumber);
        message.setFactoryNumber(serialNumber);
        byte[] reserved = new byte[RESERVED_LENGTH];
        buffer.get(reserved);
        message.setReserved(reserved);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        deviceInfo.setTerminalName(byteArrayToHexString(message.getMonitoringTerminalName()));
        deviceInfo.setTerminalType(byteArrayToHexString(message.getMonitoringTerminalModel()));
        deviceInfo.setTerminalEdition(floatToHexString(message.getMonitoringTerminalInfoVersion()));
        deviceInfo.setProducer(byteArrayToHexString(message.getManufacturer()));
        deviceInfo.setProducerCode(byteArrayToHexString(message.getFactoryNumber()));
        deviceInfo.setProducerTime(floatToHexString(message.getProductionDate()));
        deviceInfo.setCollectionTime(parseDateTimeToInst(dateTime));

        deviceInfoRepository.save(deviceInfo);
    }

    private void handleWorkingConditionMessage(WorkingConditionMessage message, ByteBuffer buffer, String deviceCode) {

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
        workStatus.setCollectionTime(parseDateTimeToInst(message.getUploadTime()));
        workStatus.setDeviceTemperature(shortToFloat(message.getDeviceTemperature()));
        workStatus.setLineCurrent(shortToFloat(message.getCurrentEffectiveValue()));

        workStatusRepository.save(workStatus);
    }

    private void handleDeviceFaultMessage(DeviceFaultMessage message, ByteBuffer buffer, String deviceCode) {
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
        deviceFault.setFaultDescribe(byteArrayToHexString(message.getDeviceFaultInfo()));

        deviceFaultRepository.save(deviceFault);
    }

    private void handleDeviceStatusMessage(DeviceStatusMessage message, ByteBuffer buffer, String deviceCode) {
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
        deviceStatus.setSolarChargeCurrent(shortToInt(message.getSolarChargingCurrent()));
        deviceStatus.setPhasePowerCurrent(shortToInt(message.getPhasePowerCurrent()));
        deviceStatus.setWorkVoltage(shortToInt(message.getDeviceWorkingVoltage()));
        deviceStatus.setWorkCurrent(shortToInt(message.getDeviceWorkingCurrent()));
        deviceStatus.setBatteryVoltage(shortToInt(message.getBatteryVoltage()));
        deviceStatus.setReserved(byteToInt(message.getReserved()));
        deviceStatus.setSolarPanelAVoltage(shortToInt(message.getSolarPanelAVoltage()));
        deviceStatus.setSolarPanelBVoltage(shortToInt(message.getSolarPanelBVoltage()));
        deviceStatus.setSolarPanelCVoltage(shortToInt(message.getSolarPanelCVoltage()));
        deviceStatus.setPhasePowerVoltage(shortToInt(message.getPhasePowerVoltage()));
        deviceStatus.setChipTemperature(shortToInt(message.getChipTemperature()));
        deviceStatus.setMainboardTemperature(shortToInt(message.getMainBoardTemperature()));
        deviceStatus.setSignalStrength(shortToInt(message.getDeviceSignalStrength()));
        deviceStatus.setGpsLatitude(floatToInt(message.getGpsLatitude()));
        deviceStatus.setGpsLongitude(floatToInt(message.getGpsLongitude()));

        deviceStatusRepository.save(deviceStatus);
    }

    private void handleWaveData(WaveDataMessage message, ByteBuffer buffer, byte[] dateTime, String deviceCode) {
        message.setDataPacketLength(buffer.getShort());
        byte[] wave = new byte[message.getDataPacketLength()];
        buffer.get(wave);
        message.setWaveData(wave);
        byte[] waveStartTime = new byte[TIME_LENGTH];
        buffer.get(waveStartTime);
        message.setWaveStartTime(waveStartTime);
        message.setWaveDataLength(buffer.getShort());
        message.setSegmentNumber(buffer.get());
        message.setDataPacketNumber(buffer.get());
        int remaining = buffer.remaining();
        byte[] reserved = new byte[remaining];
        buffer.get(reserved);
        message.setReserved(reserved);

        byte segmentNumber = message.getSegmentNumber();
        byte dataPacketNumber = message.getDataPacketNumber();

        if (segmentNumber == 1) {
            waveData = new WaveData();
            setWaveDataProperties(waveData, message, dateTime, deviceCode);
            if (dataPacketNumber == 1) {
                waveDataRepository.save(waveData);
            }
        } else {
            if (segmentNumber < dataPacketNumber) {
                appendWaveData(waveData, message);
            }
            if (segmentNumber == dataPacketNumber) {
                appendWaveData(waveData, message);
                waveDataRepository.save(waveData);
            }
        }
    }

    private void setWaveDataProperties(WaveData waveData, WaveDataMessage message, byte[] dateTime, String deviceCode) {
        waveData.setCollectionTime(parseDateTimeToInst(dateTime));
        byte messageType = message.getMessageType();
        waveData.setType(byteToInt(messageType));

        String waveDataCode = generateWaveDataCode(dateTime, byteToHexString(messageType), deviceCode);
        waveData.setCode(waveDataCode);

        waveData.setLength(shortToLong(message.getWaveDataLength()));
        waveData.setHeadTime(parseDateTimeToStr(message.getWaveStartTime()));
        waveData.setSampleRate(hyConfigProperty.getConstant().getTravelSampleRate());
        waveData.setThreshold(hyConfigProperty.getConstant().getTravelThreshold());

        int relafalg = parseRelaFalg();
        waveData.setRelaFlag(relafalg);

        waveData.setData(byteArrayToHexString(message.getWaveData()));
        waveData.setRemark(byteArrayToHexString(message.getReserved()));
    }

    public static String generateWaveDataCode(byte[] dateTime, String messageType, String deviceCode) {
        String ymd = String.format("20%02d%02d%02d", dateTime[0] & 0xFF, dateTime[1] & 0xFF, dateTime[2] & 0xFF);
        String hms = String.format("%02d%02d%02d", dateTime[3] & 0xFF, dateTime[4] & 0xFF, dateTime[5] & 0xFF);
        String ns = String.format("%03d%03d%03d",
                ((dateTime[6] & 0xFF) << 8) + (dateTime[7] & 0xFF),
                ((dateTime[8] & 0xFF) << 8) + (dateTime[9] & 0xFF),
                ((dateTime[10] & 0xFF) << 8) + (dateTime[11] & 0xFF)
        );

        return String.format("W%s-%s-%s-%s-%s", ymd, hms, ns, messageType, deviceCode);
    }



    //todo 需结合算法
    private Integer parseRelaFalg() {
        return 1;
    }

    private void appendWaveData(WaveData waveData, WaveDataMessage waveDataMessage) {
        waveData.setData(waveData.getData() + byteArrayToHexString(waveDataMessage.getWaveData()));
    }

}


