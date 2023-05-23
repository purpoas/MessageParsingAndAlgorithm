package com.hy.biz.dataPush;

import com.google.gson.*;
import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.biz.dataResolver.dto.*;
import com.hy.biz.dataResolver.exception.MessageParsingException;
import com.hy.biz.dataResolver.parser.ParserHelper;
import com.hy.domain.*;
import com.hy.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.hy.biz.dataResolver.constants.MessageConstants.MESSAGE_TO_ENTITY_ERROR;

@Slf4j
@Component
public class HyDataPushServiceImpl implements DataPushService {

    private final ParserHelper parserHelper;
    private final WaveDataRepository waveDataRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceFaultRepository deviceFaultRepository;
    private final WorkStatusRepository workStatusRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final DeviceStatusRepository deviceStatusRepository;

    public HyDataPushServiceImpl(ParserHelper parserHelper, WaveDataRepository waveDataRepository, DeviceRepository deviceRepository, DeviceFaultRepository deviceFaultRepository, DeviceInfoRepository deviceInfoRepository, DeviceStatusRepository deviceStatusRepository,
                                 WorkStatusRepository workStatusRepository) {
        this.parserHelper = parserHelper;
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
        MessageDTO messageDTO = parserHelper.createMessageDTO(data);
        long timeStamp = messageDTO.getTimeStamp();
        String deviceCode = messageDTO.getDeviceCode();

        boolean pushable = false;
        switch (dataType) {
            case HEARTBEAT:
                pushable = true;
                break;
            case DEVICE_FAULT:
                pushable = saveDeviceFault((DeviceFaultMessage) message, deviceCode);
                break;
            case DEVICE_INFO:
                pushable = handleDeviceInfo((DeviceInfoMessage) message, timeStamp, deviceCode);
                break;
            case DEVICE_STATUS:
                pushable = handleDeviceStatus((DeviceStatusMessage) message, deviceCode);
                break;
            case WORK_STATUS:
                pushable = saveWorkStatus((WorkStatusMessage) message, deviceCode);
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

//     私有方法 =========================================================================================================

    private boolean saveDeviceFault(DeviceFaultMessage message, String deviceCode) {

        DeviceFault deviceFault;
        try {
            deviceFault = message.transform(deviceRepository.findDeviceIdByCode(deviceCode));
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }
        deviceFaultRepository.save(deviceFault);

        return true;
    }

    private boolean handleDeviceInfo(DeviceInfoMessage message, long timeStamp, String deviceCode) {

        DeviceInfo deviceInfo;
        try {
            deviceInfo = message.transform(deviceRepository.findDeviceIdByCode(deviceCode), timeStamp);
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }
        deviceInfoRepository.save(deviceInfo);

        parserHelper.maintainDeviceStatus(deviceInfo, deviceCode);

        return true;
    }

    private boolean handleDeviceStatus(DeviceStatusMessage message, String deviceCode) {

        DeviceStatus deviceStatus;
        try {
            deviceStatus = message.transform(deviceRepository.findDeviceIdByCode(deviceCode));
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }
        deviceStatusRepository.save(deviceStatus);

        parserHelper.maintainDeviceStatus(deviceStatus, deviceCode);

        return true;
    }

    private boolean saveWorkStatus(WorkStatusMessage message, String deviceCode) {

        WorkStatus workStatus;
        try {
            workStatus = message.transform(deviceRepository.findDeviceIdByCode(deviceCode));
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }
        workStatusRepository.save(workStatus);

        return true;
    }

    private boolean handleWaveData(WaveDataMessage message, long timeStamp, String deviceCode) {

        WaveData waveData;
        try {
            waveData = message.transform(parserHelper, timeStamp, deviceCode);
        } catch (Exception e) {
            throw new MessageParsingException(MESSAGE_TO_ENTITY_ERROR, e);
        }

        waveDataRepository.save(waveData);

        publishWaveData(waveData, message, timeStamp, deviceCode);

        return true;
    }

    private void publishWaveData(WaveData waveData, WaveDataMessage message, long timeStamp, String deviceCode) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(
                        DateTimeFormatter.ofPattern("yyyy:MM:dd'T'HH:mm:ss'Z'")
                                .withZone(ZoneId.of("Z"))
                                .format(src)))
                .create();

        JsonObject waveDataJson = JsonParser.parseString(gson.toJson(waveData)).getAsJsonObject();

        JsonObject params = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : waveDataJson.entrySet()) {
            params.add(entry.getKey(), entry.getValue());
        }

        parserHelper.publishToRedis(message.getFrameType(), message.getMessageType(), timeStamp, deviceCode, params);
    }


}
