package com.hy.biz.dataPush;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.biz.dataResolver.dto.*;
import com.hy.biz.dataResolver.dto.MessageDTO;
import com.hy.biz.dataResolver.exception.MessageParsingException;
import com.hy.biz.dataResolver.util.WaveDataParserHelper;
import com.hy.domain.*;
import com.hy.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.hy.biz.dataResolver.constants.MessageConstants.JSON_TO_DTO_ERROR;
import static com.hy.biz.dataResolver.constants.MessageConstants.MESSAGE_TO_ENTITY_ERROR;

@Slf4j
@Component
public class HyDataPushServiceImpl implements DataPushService {

    private final ObjectMapper mapper = new ObjectMapper();
    private WaveData waveData;

    private final WaveDataParserHelper waveDataParserHelper;
    private final WaveDataRepository waveDataRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceFaultRepository deviceFaultRepository;
    private final WorkStatusRepository workStatusRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final DeviceStatusRepository deviceStatusRepository;

    public HyDataPushServiceImpl(WaveDataParserHelper waveDataParserHelper, WaveDataRepository waveDataRepository, DeviceRepository deviceRepository, DeviceFaultRepository deviceFaultRepository, DeviceInfoRepository deviceInfoRepository, DeviceStatusRepository deviceStatusRepository,
                                 WorkStatusRepository workStatusRepository) {
        this.waveDataParserHelper = waveDataParserHelper;
        this.waveDataRepository = waveDataRepository;
        this.deviceRepository = deviceRepository;
        this.deviceFaultRepository = deviceFaultRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceStatusRepository = deviceStatusRepository;
        this.workStatusRepository = workStatusRepository;
    }

    @Override
    public boolean push(String data, BaseMessage message, PushDataType dataType) {

        if (message == null) return false;
        MessageDTO messageDTO;
        try {
            messageDTO = mapper.readValue(data, MessageDTO.class);
        } catch (JsonProcessingException e) {
            throw new MessageParsingException(JSON_TO_DTO_ERROR, e);
        }

        long timeStamp = messageDTO.getTimeStamp();
        String deviceCode = messageDTO.getDeviceCode();

        boolean pushable = false;
        switch (dataType) {
            case HEARTBEAT:
                pushable = true;
                break;
            case DEVICE_FAULT:
                pushable = handleDeviceFault((DeviceFaultMessage) message, deviceCode);
                break;
            case DEVICE_INFO:
                pushable = handleDeviceInfo((DeviceInfoMessage) message, timeStamp, deviceCode);
                break;
            case DEVICE_STATUS:
                pushable = handleDeviceStatus((DeviceStatusMessage) message, deviceCode);
                break;
            case WORK_STATUS:
                pushable = handleWorkStatus((WorkStatusMessage) message, timeStamp, deviceCode);
                break;
            case WAVE:
                pushable = handleWaveData((WaveDataMessage) message, timeStamp, deviceCode);
                break;
            default:
                break;
        }

        return pushable;
    }

    @Override
    public DeviceDTO findDeviceByCode(String deviceCode) {
        Device device = deviceRepository.findDeviceByCodeAndDeletedFalse(deviceCode);
        return new DeviceDTO().from(device);
    }


//     私有方法 ====================================================================

    private boolean handleDeviceFault(DeviceFaultMessage message, String deviceCode) {
        DeviceFault deviceFault;
        try {
            deviceFault = message.transform(findDeviceByCode(deviceCode).getDeviceId());
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }
        deviceFaultRepository.save(deviceFault);

        return true;
    }

    private boolean handleDeviceInfo(DeviceInfoMessage message, long timeStamp, String deviceCode) {
        DeviceInfo deviceInfo;
        try {
            deviceInfo = message.transform(findDeviceByCode(deviceCode).getDeviceId(), timeStamp);
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }
        deviceInfoRepository.save(deviceInfo);

        return true;
    }

    private boolean handleDeviceStatus(DeviceStatusMessage message, String deviceCode) {
        DeviceStatus deviceStatus;
        try {
            deviceStatus = message.transform(findDeviceByCode(deviceCode).getDeviceId());
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }
        deviceStatusRepository.save(deviceStatus);

        return true;
    }

    private boolean handleWorkStatus(WorkStatusMessage message, long timeStamp, String deviceCode) {
        WorkStatus workStatus;
        try {
            workStatus = message.transform(findDeviceByCode(deviceCode).getDeviceId(), timeStamp);
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }
        workStatusRepository.save(workStatus);

        return true;
    }

    private boolean handleWaveData(WaveDataMessage message, long timeStamp, String deviceCode) {

        try {
            waveData = message.transform(waveData, waveDataParserHelper, timeStamp, deviceCode);
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }

        if (message.getSegmentNumber() == message.getDataPacketNumber())
            waveDataRepository.save(waveData);

        return true;
    }


}
