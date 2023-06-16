package com.hy.biz.dataPush;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hy.biz.dataParsing.dto.*;
import com.hy.biz.dataParsing.exception.MessageParsingException;
import com.hy.biz.dataParsing.parser.ParserHelper;
import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.LineDTO;
import com.hy.biz.dataPush.dto.PoleDTO;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.domain.*;
import com.hy.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.hy.biz.dataParsing.constants.MessageConstants.MESSAGE_TO_ENTITY_ERROR;

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
    private final CircuitPathRepository circuitPathRepository;
    private final PoleRepository poleRepository;
    private final OrgRepository orgRepository;

    public HyDataPushServiceImpl(ParserHelper parserHelper, WaveDataRepository waveDataRepository, DeviceRepository deviceRepository, DeviceFaultRepository deviceFaultRepository, DeviceInfoRepository deviceInfoRepository, DeviceStatusRepository deviceStatusRepository,
                                 WorkStatusRepository workStatusRepository, CircuitPathRepository circuitPathRepository, PoleRepository poleRepository, OrgRepository orgRepository) {
        this.parserHelper = parserHelper;
        this.waveDataRepository = waveDataRepository;
        this.deviceRepository = deviceRepository;
        this.deviceFaultRepository = deviceFaultRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceStatusRepository = deviceStatusRepository;
        this.workStatusRepository = workStatusRepository;
        this.circuitPathRepository = circuitPathRepository;
        this.poleRepository = poleRepository;
        this.orgRepository = orgRepository;
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
        if (device == null) return null;

        return new DeviceDTO().from(device);
    }

    @Override
    public LineDTO findLineByLineId(String lineId) {
        Org line = orgRepository.findByIdAndDeletedFalse(Long.valueOf(lineId));

        if (line == null || !StringUtils.hasText(line.getProperties())) return null;

        JsonObject properties = JsonParser.parseString(line.getProperties()).getAsJsonObject();

        if (!properties.has("length") || properties.get("length").isJsonNull()) return null;

        return new LineDTO(lineId, properties.get("length").getAsDouble());
    }

    @Override
    public List<PoleDTO> findPolesByLineId(String lineId) {

        List<Pole> poleList = poleRepository.findByOrgIdAndDeletedFalse(Long.valueOf(lineId));

        if(CollectionUtils.isEmpty(poleList)) return null;

        return poleList.stream().map(PoleDTO::new).collect(Collectors.toList());
    }

    @Override
    public PoleDTO getPoleByPoleId(String lineId, String poleId) {
        Pole pole = poleRepository.findByIdAndDeletedFalse(Long.valueOf(poleId));

        if (pole == null) return null;

        // 线路隶属关系结构
        List<Long> circuitPathList = circuitPathRepository.findByAllDescendantByLineId(Long.valueOf(lineId));

        if (CollectionUtils.isEmpty(circuitPathList)) return null;

        // 找出depth最大的线路Id
        String topMainLineId = String.valueOf(circuitPathList.get(circuitPathList.size() - 1));

        // 计算当前线路所属杆塔到起始杆塔的距离
        Double d1 = poleRepository.calculatePoleDistanceToHeadStation(Long.valueOf(lineId), pole.getOrderNum());

        // 查询线路Id和主线Id相同 直接返回杆塔到起始变电站距离
        if (topMainLineId.equals(lineId)) {
            return new PoleDTO(topMainLineId, lineId, poleId, pole.getOrderNum(), d1);
        }

        double distance = d1;

        // 最大层级的元素不进行计算距离
        for (int i = 0; i < circuitPathList.size() - 1; i++) {
            Long item = circuitPathList.get(i);
            CircuitPath parentPath = circuitPathRepository.findByDescendantAndDepthFix1(item);

            if (parentPath == null) continue;

            Pole parentPole = poleRepository.findByIdAndDeletedFalse(parentPath.getHeadPole());

            if (parentPole == null) continue;

            Double d = poleRepository.calculatePoleDistanceToHeadStation(parentPath.getAncestor(), parentPole.getOrderNum());
            distance = distance + d;
        }

        return new PoleDTO(topMainLineId, lineId, poleId, pole.getOrderNum(), distance);
    }


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

        parserHelper.maintainDeviceStatusInRedis(deviceInfo, deviceCode);

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

        parserHelper.maintainDeviceStatusInRedis(deviceStatus, deviceCode);

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

        parserHelper.publishWaveData(waveData, message, timeStamp, deviceCode);

        return true;
    }


}
