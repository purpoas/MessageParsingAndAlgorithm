package com.hy.biz.dataPush;

import com.google.gson.*;
import com.hy.biz.cache.service.GroundFaultCacheManager;
import com.hy.config.AnalysisConstants;
import com.hy.biz.dataAnalysis.dto.FaultAnalysisResultDTO;
import com.hy.biz.dataAnalysis.featureAlgorithm.FaultFeatureAlgorithm;
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
    private final DeviceAlarmRepository deviceAlarmRepository;
    private final GroundFaultCacheManager groundFaultCacheManager;

    public HyDataPushServiceImpl(ParserHelper parserHelper, WaveDataRepository waveDataRepository, DeviceRepository deviceRepository, DeviceFaultRepository deviceFaultRepository, DeviceInfoRepository deviceInfoRepository, DeviceStatusRepository deviceStatusRepository,
                                 WorkStatusRepository workStatusRepository, CircuitPathRepository circuitPathRepository, PoleRepository poleRepository, OrgRepository orgRepository, DeviceAlarmRepository deviceAlarmRepository, GroundFaultCacheManager groundFaultCacheManager) {
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
        this.deviceAlarmRepository = deviceAlarmRepository;
        this.groundFaultCacheManager = groundFaultCacheManager;
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
    public boolean pushFaultAlarm(FaultAnalysisResultDTO faultAnalysisResult) {

        DeviceAlarm alarm = new DeviceAlarm();
        alarm.setFaultLineId(Long.valueOf(faultAnalysisResult.getFaultLineId()));
        alarm.setFaultTime(faultAnalysisResult.getFaultTime());
        alarm.setFaultType(faultAnalysisResult.getFaultType());
        alarm.setFaultWaveSets(faultAnalysisResult.getFaultWaveSets());

        alarm.setFaultPoleId(StringUtils.hasText(faultAnalysisResult.getNearestPoleId()) ? Long.valueOf(faultAnalysisResult.getNearestPoleId()) : null);
        alarm.setDistToFaultPole(faultAnalysisResult.getDistToFaultPole());
        alarm.setFaultHeadPoleId(StringUtils.hasText(faultAnalysisResult.getFaultHeadPoleId()) ? Long.valueOf(faultAnalysisResult.getFaultHeadPoleId()) : null);
        alarm.setFaultEndPoleId(StringUtils.hasText(faultAnalysisResult.getFaultEndPoleId()) ? Long.valueOf(faultAnalysisResult.getFaultEndPoleId()) : null);

        // 故障区间描述
        if (StringUtils.hasText(faultAnalysisResult.getFaultHeadPoleId()) && StringUtils.hasText(faultAnalysisResult.getFaultEndPoleId())) {
            Pole headPole = poleRepository.findByIdAndDeletedFalse(Long.valueOf(faultAnalysisResult.getFaultHeadPoleId()));
            Pole endPole = poleRepository.findByIdAndDeletedFalse(Long.valueOf(faultAnalysisResult.getFaultEndPoleId()));

            StringBuilder faultArea = new StringBuilder();

            Org headOrg = headPole.getOrg();
            Org endOrg = endPole.getOrg();

            if (headOrg != null) {
                faultArea.append(JsonParser.parseString(headOrg.getProperties()).getAsJsonObject().get("name").getAsString()).append(headOrg.getName()).append(" - ");
            }

            if (endOrg != null) {
                faultArea.append(JsonParser.parseString(endOrg.getProperties()).getAsJsonObject().get("name").getAsString()).append(endOrg.getName());
            }

            alarm.setFaultArea(faultArea.toString());
        }

        // 故障距离描述
        if (StringUtils.hasText(faultAnalysisResult.getNearestPoleId())) {
            StringBuilder faultDistance = new StringBuilder();

            Pole nearestPole = poleRepository.findByIdAndDeletedFalse(Long.valueOf(faultAnalysisResult.getNearestPoleId()));

            Org nearestOrg = nearestPole.getOrg();

            if (nearestOrg != null) {
                faultDistance.append(JsonParser.parseString(nearestOrg.getProperties()).getAsJsonObject().get("name").getAsString()).append(nearestOrg.getName());
            }

            faultDistance.append(nearestPole.getName()).append("附近").append(faultAnalysisResult.getDistToFaultPole()).append("米");
            alarm.setFaultDistance(faultDistance.toString());
        }

        // 故障特征描述
        if (StringUtils.hasText(faultAnalysisResult.getFaultFeature())) {
            String faultFeature = FaultFeatureAlgorithm.createFaultFeatureDescription(faultAnalysisResult.getFaultFeature());
            faultAnalysisResult.setFaultFeature(faultFeature);
        }

        alarm = deviceAlarmRepository.save(alarm);

        // 判断是否是接地故障 是则放入Ground Cache中
        if (AnalysisConstants.FAULT_NATURE_GROUND_A.equals(faultAnalysisResult.getFaultType()) || AnalysisConstants.FAULT_NATURE_GROUND_B.equals(faultAnalysisResult.getFaultType()) || AnalysisConstants.FAULT_NATURE_GROUND_C.equals(faultAnalysisResult.getFaultType())) {
            faultAnalysisResult.setSerial(alarm.getId());
            groundFaultCacheManager.put(faultAnalysisResult.getFaultLineId(), faultAnalysisResult);
        }

        return true;
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

        if (CollectionUtils.isEmpty(poleList)) return null;

        return poleList.stream().map(PoleDTO::new).collect(Collectors.toList());
    }

    @Override
    public PoleDTO getPoleByPoleId(String lineId, String poleId) {
        Pole pole = poleRepository.findByIdAndDeletedFalse(Long.valueOf(poleId));

        if (pole == null) return null;

        // 线路隶属关系结构
        List<CircuitPath> circuitPathList = circuitPathRepository.findByAllCircuitPathByLineId(Long.valueOf(lineId));

        if (CollectionUtils.isEmpty(circuitPathList)) return null;

        // 找出depth最大的线路Id
        String mainLineId = String.valueOf(circuitPathList.get(circuitPathList.size() - 1).getAncestor());
        Integer lineDepth = circuitPathList.get(circuitPathList.size() - 1).getDepth();

        // 计算当前线路所属杆塔到起始杆塔的距离
        Double d1 = poleRepository.calculatePoleDistanceToHeadStation(Long.valueOf(lineId), pole.getOrderNum());

        // 查询线路Id和主线Id相同 直接返回杆塔到起始变电站距离
        if (mainLineId.equals(lineId)) {
            return new PoleDTO(mainLineId, lineId, lineDepth, poleId, pole.getOrderNum(), d1);
        }

        double distance = d1;

        // 最大层级的元素不进行计算距离
        for (int i = 0; i < circuitPathList.size() - 1; i++) {
            Long item = circuitPathList.get(i).getAncestor();
            CircuitPath parentPath = circuitPathRepository.findByDescendantAndDepthFix1(item);

            if (parentPath == null) continue;

            Pole parentPole = poleRepository.findByIdAndDeletedFalse(parentPath.getHeadPole());

            if (parentPole == null) continue;

            Double d = poleRepository.calculatePoleDistanceToHeadStation(parentPath.getAncestor(), parentPole.getOrderNum());
            distance = distance + d;
        }

        return new PoleDTO(mainLineId, lineId, lineDepth, poleId, pole.getOrderNum(), distance);
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
