package com.hy.biz.MessageParsing;

import com.hy.biz.MessageParsing.entity.*;
import com.hy.biz.MessageParsing.exception.MessageParserException;
import com.hy.domain.*;
import com.hy.repository.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import static com.hy.biz.MessageParsing.constants.MessageConstants.*;
import static com.hy.biz.MessageParsing.util.DataTypeConverter.byteArrayToString;
import static com.hy.biz.MessageParsing.util.DataTypeConverter.hexStringToByteArray;
import static com.hy.biz.MessageParsing.util.DateTimeUtil.parseDateTimeToInst;
import static com.hy.biz.MessageParsing.util.DateTimeUtil.parseDateTimeToStr;


/**
 * MessageParser 负责解析设备上传的数据，并执行相应地入库、响应等操作
 */
@Component
@Transactional
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MessageParser {
    private final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    private final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = MessageClassRegistry.getMessageMap();

    @Value("${hy.constant.frequency-sample-rate}")
    private Long FREQUENCY_SAMPLE_RATE;

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

    /**
     * @description       Parses the command data from the device.
     * @param dateTime    the date and time when the message was received.
     * @param commandData the command data in hexadecimal format.
     * @param deviceCode  the device code.
     */
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

    }

    private BaseMessage createSpecificMessage(String key, String deviceCode, byte messageType) {
        Class<? extends BaseMessage> messageClass = MESSAGE_MAP.get(key);
        BaseMessage specificMessage;
        try {
            specificMessage = messageClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MessageParserException("无法从 MESSAGE_MAP 中创建对应的报文实体类", e);
        }
        specificMessage.setIdNumber(deviceCode.getBytes());
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
            handleDeviceFaultMessage((DeviceFaultMessage) specificMessage, buffer, deviceCode);
        } else if (specificMessage instanceof DeviceStatusMessage) {
            handleDeviceStatusMessage((DeviceStatusMessage) specificMessage, buffer, deviceCode);
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
        deviceInfo.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));
        deviceInfo.setTerminalName(byteArrayToString(message.getMonitoringTerminalName()));
        deviceInfo.setTerminalType(byteArrayToString(message.getMonitoringTerminalModel()));
        deviceInfo.setTerminalEdition(String.valueOf(message.getMonitoringTerminalInfoVersion()));
        deviceInfo.setProducer(byteArrayToString(message.getManufacturer()));
        deviceInfo.setProducerCode(byteArrayToString(message.getFactoryNumber()));
        deviceInfo.setProducerTime(String.valueOf(message.getProductionDate()));
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
        workStatus.setDeviceTemperature((float) message.getDeviceTemperature());
        workStatus.setLineCurrent((float) message.getCurrentEffectiveValue());

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
        deviceFault.setFaultDescribe(byteArrayToString(message.getDeviceFaultInfo()));

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
        waveData.setHeadTime(parseDateTimeToStr(waveDataMessage.getWaveStartTime()));
        waveData.setSampleRate(FREQUENCY_SAMPLE_RATE); // Set appropriate sample rate
        waveData.setThreshold(null); // Set appropriate threshold
        waveData.setRelaFlag(null); // Set appropriate relaFlag
        waveData.setData(byteArrayToString(waveDataMessage.getWaveData()));
        waveData.setRemark(byteArrayToString(waveDataMessage.getReserved()));
    }

    private void appendWaveData(WaveData waveData, WaveDataMessage waveDataMessage) {
        waveData.setData(waveData.getData() + byteArrayToString(waveDataMessage.getWaveData()));
    }

}


