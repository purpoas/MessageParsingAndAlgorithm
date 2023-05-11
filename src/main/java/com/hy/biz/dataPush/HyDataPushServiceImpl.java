package com.hy.biz.dataPush;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.biz.dataResolver.entity.*;
import com.hy.biz.dataResolver.entity.dto.MessageDTO;
import com.hy.biz.dataResolver.util.WaveDataParserHelper;
import com.hy.domain.*;
import com.hy.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HyDataPushServiceImpl implements DataPushService {

    private final ObjectMapper mapper = new ObjectMapper();
    private WaveData waveData;

    private final WaveDataParserHelper waveDataParserHelper;
    private final DeviceRepository deviceRepository;
    private final DeviceFaultRepository deviceFaultRepository;
    private final WorkStatusRepository workStatusRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final DeviceStatusRepository deviceStatusRepository;

    public HyDataPushServiceImpl(WaveDataParserHelper waveDataParserHelper, DeviceRepository deviceRepository, DeviceFaultRepository deviceFaultRepository, DeviceInfoRepository deviceInfoRepository, DeviceStatusRepository deviceStatusRepository,
                                 WorkStatusRepository workStatusRepository) {
        this.waveDataParserHelper = waveDataParserHelper;
        this.deviceRepository = deviceRepository;
        this.deviceFaultRepository = deviceFaultRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceStatusRepository = deviceStatusRepository;
        this.workStatusRepository = workStatusRepository;
    }

    @Override
    public boolean push(String data, BaseMessage message, PushDataType dataType) {

        MessageDTO messageDTO;
        try {
            messageDTO = mapper.readValue(data, MessageDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        long timeStamp = messageDTO.getTimeStamp();
        String deviceCode = messageDTO.getDeviceCode();

        boolean pushable = false;
        if (message == null) return pushable;
        switch (dataType) {
            case HEARTBEAT:
                log.info("接收到心跳数据");
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
    public boolean deviceExists(String deviceCode) {
        return deviceRepository.findDeviceByCodeAndDeletedFalse(deviceCode) != null;
    }

    @Override
    public DeviceDTO findDeviceByCode(String deviceCode) {
        Device device = deviceRepository.findDeviceByCodeAndDeletedFalse(deviceCode);
        return new DeviceDTO().from(device);
    }


//     私有方法 ====================================================================

    private boolean handleDeviceFault(DeviceFaultMessage message, String deviceCode) {
        DeviceFault deviceFault = message.transform(findDeviceByCode(deviceCode).getDeviceId());
        deviceFaultRepository.save(deviceFault);
        return true;
    }

    private boolean handleDeviceInfo(DeviceInfoMessage message, long timeStamp, String deviceCode) {
        DeviceInfo deviceInfo = message.transform(findDeviceByCode(deviceCode).getDeviceId(), timeStamp);
        deviceInfoRepository.save(deviceInfo);
        return true;
    }

    private boolean handleDeviceStatus(DeviceStatusMessage message, String deviceCode) {
        DeviceStatus deviceStatus = message.transform(findDeviceByCode(deviceCode).getDeviceId());
        deviceStatusRepository.save(deviceStatus);
        return true;
    }

    private boolean handleWorkStatus(WorkStatusMessage message, long timeStamp, String deviceCode) {
        WorkStatus workStatus = message.transform(findDeviceByCode(deviceCode).getDeviceId(), timeStamp);
        workStatusRepository.save(workStatus);
        return true;
    }

    private boolean handleWaveData(WaveDataMessage message, long timeStamp, String deviceCode) {
        message.transform(waveData, waveDataParserHelper, timeStamp, deviceCode);
        return true;
    }

}
