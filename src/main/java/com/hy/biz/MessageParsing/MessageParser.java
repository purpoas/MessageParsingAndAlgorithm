package com.hy.biz.MessageParsing;

import com.hy.biz.MessageParsing.entity.*;
import com.hy.biz.MessageParsing.exception.MessageParserException;
import com.hy.biz.MessageParsing.util.DateTimeUtil;
import com.hy.domain.*;
import com.hy.repository.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static com.hy.biz.MessageParsing.constants.FrameType.MONITORING_DATA_REPORT;
import static com.hy.biz.MessageParsing.constants.FrameType.WORK_STATUS_REPORT;
import static com.hy.biz.MessageParsing.constants.MessageConstants.*;
import static com.hy.biz.MessageParsing.constants.MessageType.*;
import static com.hy.biz.MessageParsing.util.DataTypeConverter.byteArrayToString;
import static com.hy.biz.MessageParsing.util.DataTypeConverter.hexStringToByteArray;
import static com.hy.biz.MessageParsing.util.DateTimeUtil.parseDateTimeToInst;


@Service
@Transactional
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MessageParser {
    private static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    private static final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = new HashMap<>();
    private final WaveDataRepository waveDataRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final WorkStatusRepository workStatusRepository;
    private final DeviceFaultRepository deviceFaultRepository;
    private final DeviceStatusRepository deviceStatusRepository;
    private final DeviceRepository deviceRepository;
    private WaveData waveData;

    public MessageParser(WaveDataRepository waveDataRepository, DeviceInfoRepository deviceInfoRepository, WorkStatusRepository workStatusRepository, DeviceFaultRepository deviceFaultRepository, DeviceStatusRepository deviceStatusRepository, DeviceRepository deviceRepository) {
        this.waveDataRepository = waveDataRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.workStatusRepository = workStatusRepository;
        this.deviceFaultRepository = deviceFaultRepository;
        this.deviceStatusRepository = deviceStatusRepository;
        this.deviceRepository = deviceRepository;
    }

    static {
        registerMessageClass();
    }

    private static void registerMessageClass() {
        MESSAGE_MAP.put(MONITORING_DATA_REPORT + ":" + TRAVELLING_WAVE_CURRENT, TravellingWaveCurrentMessage.class);
        MESSAGE_MAP.put(MONITORING_DATA_REPORT + ":" + FAULT_CURRENT, FaultCurrentMessage.class);
        MESSAGE_MAP.put(MONITORING_DATA_REPORT + ":" + FAULT_VOLTAGE, FaultVoltageMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + HEARTBEAT, HeartBeatMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + BASIC_INFO, BasicInfoMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + WORKING_CONDITION, WorkingConditionMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + DEVICE_FAULT, DeviceFaultMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + DEVICE_STATUS, DeviceStatusMessage.class);
    }

    public void parse(byte[] dateTime, String commandData, String deviceCode) {
        String[] commandSplit = commandData.split(",");
        int index = 0;

        while (index < commandSplit.length) {
            processMessage(commandSplit[index], dateTime, deviceCode);
            index++;
        }
    }

    private void processMessage(String hexString, byte[] dateTime, String deviceCode) {
        byte[] command = hexStringToByteArray(hexString);
        ByteBuffer buffer = ByteBuffer.wrap(command).order(BYTE_ORDER);

        if (buffer.getShort() != HEADER) {
            throw new IllegalArgumentException(ILLEGAL_HEADER_ERROR);
        }

        byte[] idNumber = new byte[ID_LENGTH];
        buffer.get(idNumber);
        byte frameType = buffer.get();
        byte messageType = buffer.get();
        String key = frameType + ":" + messageType;
        BaseMessage specificMessage = createSpecificMessage(key, idNumber, messageType);

        short messageLength = buffer.getShort();
        byte[] messageContent = new byte[messageLength];
        buffer.get(messageContent);
        parseMessageContent(messageContent, specificMessage, dateTime, deviceCode);
        buffer.getShort(); // Skip checksum

    }

    private BaseMessage createSpecificMessage(String key, byte[] idNumber, byte messageType) {
        Class<? extends BaseMessage> messageClass = MESSAGE_MAP.get(key);
        BaseMessage specificMessage;
        try {
            specificMessage = messageClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MessageParserException("无法从 MESSAGE_MAP 中创建对应的报文实体类", e);
        }
        specificMessage.setIdNumber(idNumber);
        specificMessage.setMessageType(messageType);
        return specificMessage;
    }

    private void parseMessageContent(byte[] messageContent, BaseMessage specificMessage, byte[] dateTime, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BYTE_ORDER);

        if (specificMessage instanceof HeartBeatMessage) {
            System.out.println(specificMessage);
        } else if (specificMessage instanceof BasicInfoMessage) {
            handleBasicInfoMessage((BasicInfoMessage) specificMessage, buffer, dateTime, deviceCode);
        } else if (specificMessage instanceof WorkingConditionMessage) {
            handleWorkingConditionMessage((WorkingConditionMessage) specificMessage, buffer, deviceCode);
        } else if (specificMessage instanceof DeviceFaultMessage) {
            handleDeviceFaultMessage((DeviceFaultMessage) specificMessage, buffer);
        } else if (specificMessage instanceof DeviceStatusMessage) {
            handleDeviceStatusMessage((DeviceStatusMessage) specificMessage, buffer);
        } else if (specificMessage instanceof WaveDataMessage) {
            handleWaveData((WaveDataMessage) specificMessage, buffer);
        } else {
            throw new MessageParserException("未知报文签名");
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
        deviceInfo.setTerminalName(byteArrayToString(message.getMonitoringTerminalName()));
        deviceInfo.setTerminalType(byteArrayToString(message.getMonitoringTerminalModel()));
        deviceInfo.setTerminalEdition(String.valueOf(message.getMonitoringTerminalInfoVersion()));
        deviceInfo.setProducer(byteArrayToString(message.getManufacturer()));
        deviceInfo.setProducerTime(String.valueOf(message.getProductionDate()));
        deviceInfo.setCollectionTime(parseDateTimeToInst(dateTime));
        deviceInfo.setDevice(deviceRepository.findDeviceByIdAndDeletedFalse(Long.parseLong(deviceCode)));

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
        workStatus.setDevice(deviceRepository.findDeviceByIdAndDeletedFalse(Long.parseLong(deviceCode)));
        workStatus.setCollectionTime(parseDateTimeToInst(message.getUploadTime()));
        workStatus.setDeviceTemperature((float) message.getDeviceTemperature());
        workStatus.setLineCurrent((float) message.getCurrentEffectiveValue());

        workStatusRepository.save(workStatus);
    }

    private void handleDeviceFaultMessage(DeviceFaultMessage message, ByteBuffer buffer) {
        byte[] time = new byte[TIME_LENGTH];
        buffer.get(time);
        message.setFaultDataCollectionTime(time);
        int remaining = buffer.remaining();
        byte[] info = new byte[remaining];
        buffer.get(info);
        message.setDeviceFaultInfo(info);

        DeviceFault deviceFault = new DeviceFault();
        deviceFault.setCollectionTime(parseDateTimeToInst(message.getFaultDataCollectionTime()));
        deviceFault.setFaultDescribe(byteArrayToString(message.getDeviceFaultInfo()));

        deviceFaultRepository.save(deviceFault);
    }

    private void handleDeviceStatusMessage(DeviceStatusMessage message, ByteBuffer buffer) {
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
        System.out.println(message);
        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setCollectionTime(parseDateTimeToInst(message.getDataCollectionUploadTime()));
        deviceStatus.setDeviceId(ByteBuffer.wrap(message.getIdNumber()).getLong());
        deviceStatus.setSolarChargeCurrent((int) message.getSolarChargingCurrent());
        deviceStatus.setBatteryVoltage((int) message.getBatteryVoltage());
        deviceStatus.setSignalStrength((int) message.getDeviceSignalStrength());
        deviceStatus.setChipTemperature((int) message.getChipTemperature());
        deviceStatus.setMainboardTemperature((int) message.getMainBoardTemperature());
        deviceStatus.setWorkCurrent((int) message.getDeviceWorkingCurrent());
        deviceStatus.setWorkVoltage((int) message.getDeviceWorkingVoltage());

        System.out.println(deviceStatus);
        deviceStatusRepository.save(deviceStatus);
    }

    private void handleWaveData(WaveDataMessage message, ByteBuffer buffer) {
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
            setWaveDataProperties(waveData, message);
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

    private void setWaveDataProperties(WaveData waveData, WaveDataMessage waveDataMessage) {
        waveData.setType((int) waveDataMessage.getMessageType());
        waveData.setCode(null); // Set appropriate code
        waveData.setLength((long) waveDataMessage.getDataPacketLength());
        waveData.setHeadTime(DateTimeUtil.parseDateTimeToStr(waveDataMessage.getWaveStartTime()));
        waveData.setSampleRate(null); // Set appropriate sample rate
        waveData.setThreshold(null); // Set appropriate threshold
        waveData.setRelaFlag(null); // Set appropriate relaFlag
        waveData.setData(byteArrayToString(waveDataMessage.getWaveData()));
        waveData.setRemark(byteArrayToString(waveDataMessage.getReserved()));
    }

    private void appendWaveData(WaveData waveData, WaveDataMessage waveDataMessage) {
        waveData.setData(waveData.getData() + byteArrayToString(waveDataMessage.getWaveData()));
    }

}


