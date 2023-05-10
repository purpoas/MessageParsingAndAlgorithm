package com.hy.biz.dataPush;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PushDataType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class HyDataPushServiceImpl implements DataPushService {

    private final Logger log = LoggerFactory.getLogger(HyDataPushServiceImpl.class);


    @Override
    public boolean push(String dataJson, PushDataType dataType) {
        boolean isPush = false;

        if (StringUtils.isEmpty(dataJson)) return isPush;

        switch (dataType) {
            case HEARTBEAT:
//                handleHeartBeat(dataJson);
                break;
            case ERRORLOG:
                handleErrorLog(dataJson);
                break;
            case WORK_STATE:
                isPush = handleWorkState(dataJson);
                break;
            case DEVUCE_INFO:
                isPush = handleDeviceInfo(dataJson);
                break;
            case DEVICE_PARAM:
                handleDeviceParam(dataJson);
                break;
            case WAVE:
                isPush = handleWave(dataJson);
                break;
            case FAULT_WAVE:
                handleFaultWave(dataJson);
                break;
            case ANALYSIS_RESULT:
                isPush = handleAnalysisResult(dataJson);
                break;
            case GPS:
                handleDeviceGps(dataJson);
                break;
            case DEVICE_STATUS:
                handleDeviceStatus(dataJson);
                break;
            case DEVICE_WORKHOURS:
                handleDeviceWorkHours(dataJson);
                break;
            case HISTORY_DATA:
                handleHistoryData(dataJson);
                break;
            case DEVICE_DAILY_HISTORY_DATA:
//                handleDeviceDailyHistoryData(dataJson);
                break;
            default:
                break;
        }

        return isPush;
    }

    @Override
    public boolean isExistDevice(String deviceCode) {
        return false;
    }

    @Override
    public DeviceDTO findDeviceByCode(String deviceCode) {
        return null;
    }


    // 私有方法 ====================================================================

    private void handleHeartBeat(String data) {
        StateGridHeartBeatDTO dto = GsonUtil.getInstance().fromJson(data, StateGridHeartBeatDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return;


    }

    private void handleErrorLog(String data) {
        StateGridErrorLogDTO dto = GsonUtil.getInstance().fromJson(data, StateGridErrorLogDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return;


    }

    private boolean handleWorkState(String data) {
        boolean flag = false;

        StateGridWorkStatusDTO dto = GsonUtil.getInstance().fromJson(data, StateGridWorkStatusDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());


        return flag;
    }

    private boolean handleDeviceInfo(String data) {
        boolean flag = false;

        StateGridDeviceInfoDTO dto = GsonUtil.getInstance().fromJson(data, StateGridDeviceInfoDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return flag;


        return flag;

    }

    private void handleDeviceParam(String data) {

        StateGridDeviceParamDTO dto = GsonUtil.getInstance().fromJson(data, StateGridDeviceParamDTO.class);


    }

    private boolean handleWave(String data) {
        boolean flag = false;

        WaveDataDTO dto = GsonUtil.getInstance().fromJson(data, WaveDataDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return flag;


        JsonObject wave = new JsonObject();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        wave.add("supplierCode", new JsonPrimitive(hyConfigProperty.getConstant().getSupplierCode()));
        wave.add("deviceId", new JsonPrimitive(device.getId()));
        wave.add("lineId", new JsonPrimitive(lineId));
        wave.add("towerId", new JsonPrimitive(towerId));
        wave.add("phase", new JsonPrimitive(device.getPhase()));
        wave.add("code", new JsonPrimitive(dto.getWaveId()));
        wave.add("headTime", new JsonPrimitive(DateUtil.handleHeadTimeToUs(dto.getHeadTime())));
        wave.add("length", new JsonPrimitive(dto.getLength()));
        wave.add("sampleRate", new JsonPrimitive(dto.getSampleRate()));
        wave.add("data", new JsonPrimitive(dto.getData()));
        wave.add("collectionTime", new JsonPrimitive(formatter.format(dto.getCollectionTime())));
        wave.add("relaFalg", new JsonPrimitive(dto.getRelaFalg() == null ? Constants.NATURE_WAVE_DEFAULT : dto.getRelaFalg()));

        switch (dto.getMessType()) {
            case 0x01:
                wave.add("type", new JsonPrimitive(Constants.WAVE_TRAVEL_TYPE));
                wave.add("threshold", new JsonPrimitive(hyConfigProperty.getConstant().getTravelThreshold()));
                break;
            case 0x03:
                wave.add("type", new JsonPrimitive(Constants.WAVE_FREQUENCY_TYPE));
                wave.add("threshold", new JsonPrimitive(hyConfigProperty.getConstant().getFrequencyThreshold()));
                break;
            case 0x04:
                wave.add("type", new JsonPrimitive(Constants.WAVE_DANGER_TYPE));
                wave.add("threshold", new JsonPrimitive(hyConfigProperty.getConstant().getTravelThreshold()));
                break;
            case 0x05:
                wave.add("type", new JsonPrimitive(Constants.WAVE_GROUND_TYPE));
                wave.add("threshold", new JsonPrimitive(hyConfigProperty.getConstant().getTravelThreshold()));
                break;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("WaveData", wave);
//        log.info("发送 http 请求，请求方式：{}\nurl: {}\nURL参数: {}\nBody参数: {}", "POST", hyConfigProperty.getAccess().getApi(), null, jsonObject.toString());
        HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);
        if (httpBackDTO != null && httpBackDTO.getStatusCode() == 200) {
            JsonObject backObj = JsonParser.parseString(httpBackDTO.getBody()).getAsJsonObject();
            if ("ok".equalsIgnoreCase(backObj.get("Result").getAsString())) {
                flag = true;
            }
        }
        log.info("设备： {} , 波形推送结果： {}", dto.getDeviceCode(), httpBackDTO);
        return flag;
    }

    private void handleFaultWave(String data) {

        WaveDataDTO dto = GsonUtil.getInstance().fromJson(data, WaveDataDTO.class);

        JsonObject faultWave = new JsonObject();

        faultWave.add("waveId", new JsonPrimitive(dto.getWaveId()));
        faultWave.add("relaFalg", new JsonPrimitive(dto.getRelaFalg()));

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("FaultWaveData", faultWave);
//        log.info("发送 http 请求，请求方式：{}\nurl: {}\nURL参数: {}\nBody参数: {}", "POST", hyConfigProperty.getAccess().getApi(), null, jsonObject.toString());
        HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);

        log.info("波形编号： {} , 故障波形推送结果： {}", dto.getWaveId(), httpBackDTO);

    }

    private boolean handleAnalysisResult(String data) {
        boolean flag = false;

        AnalysisResultDTO dto = GsonUtil.getInstance().fromJson(data, AnalysisResultDTO.class);

        JsonObject faultRes = new JsonObject();

        faultRes.add("faultWaveSets", new JsonPrimitive(dto.getFaultWaveSets()));
        faultRes.add("faultTime", new JsonPrimitive(DateUtil.handleHeadTimeToUs(dto.getFaultTime())));
        faultRes.add("faultLineId", new JsonPrimitive(dto.getFaultLineId()));

        faultRes.addProperty("faultTowerId", dto.getFaultTowerId());
        faultRes.addProperty("faultHeadTowerId", dto.getFaultHeadTowerId());
        faultRes.addProperty("faultEndTowerId", dto.getFaultEndTowerId());

        faultRes.addProperty("distanceToHeadStation", dto.getDistanceToHeadStation());
        faultRes.addProperty("distanceToEndStation", dto.getDistanceToEndStation());

        faultRes.add("faultPhase", new JsonPrimitive(dto.getFaultPhase()));
        faultRes.add("isLight", new JsonPrimitive(dto.getIsLight()));
        faultRes.add("lightType", new JsonPrimitive(dto.getLightType()));
        faultRes.add("isGate", new JsonPrimitive(dto.getIsGate()));
        faultRes.add("faultDescribe", new JsonPrimitive(dto.getFaultDescribe()));

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("FaultRes", faultRes);
        log.info("发送 http 请求，请求方式：{}\nurl: {}\nURL参数: {}\nBody参数: {}", "POST", hyConfigProperty.getAccess().getApi(), null, jsonObject.toString());
        HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);
        if (httpBackDTO != null && httpBackDTO.getStatusCode() == 200) {
            JsonObject backObj = JsonParser.parseString(httpBackDTO.getBody()).getAsJsonObject();
            if ("ok".equalsIgnoreCase(backObj.get("Result").getAsString())) {
                flag = true;
            }
        } else {
            FileUtil.writeFileContent(hyConfigProperty.getConstant().getFaultFilePath(), System.currentTimeMillis() + ".txt", jsonObject.toString());
        }
        log.info("故障分析推送结果： {}", httpBackDTO);
        return flag;
    }


    private void handleDeviceGps(String data) {

        StateGridDeviceGpsDTO dto = GsonUtil.getInstance().fromJson(data, StateGridDeviceGpsDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return;

        Long lineId = 0L;
        Long towerId = 0L;

        if (device.getPole() != null) {
            towerId = device.getPole().getId();
            if (device.getPole().getCircuit() != null) {
                lineId = device.getPole().getCircuit().getId();
            }
        }

        JsonObject deviceGps = new JsonObject();
        deviceGps.add("supplierCode", new JsonPrimitive(hyConfigProperty.getConstant().getSupplierCode()));
        deviceGps.add("deviceId", new JsonPrimitive(device.getId()));
        deviceGps.add("deviceCode", new JsonPrimitive(device.getCode()));
        deviceGps.add("type", new JsonPrimitive("deviceGps"));
        deviceGps.add("lineId", new JsonPrimitive(lineId));
        deviceGps.add("towerId", new JsonPrimitive(towerId));
        deviceGps.add("phase", new JsonPrimitive(device.getPhase()));
        if (dto.getUploadTime().equals("000000000000000000000000")) {
            deviceGps.add("uploadTime", new JsonPrimitive("0000-00-00 00:00:00.000000000"));
        } else {
            String uploadTime = hexTimeToTimeString(dto.getUploadTime(), null);
            deviceGps.add("uploadTime", new JsonPrimitive(StringUtils.isEmpty(uploadTime) ? "0000-00-00 00:00:00.000000000" : uploadTime));
        }
        deviceGps.add("longitudeDegree", new JsonPrimitive(dto.getLongitudeDegree()));
        deviceGps.add("longitudeFraction", new JsonPrimitive(dto.getLongitudeFraction()));
        deviceGps.add("latitudeDegree", new JsonPrimitive(dto.getLatitudeDegree()));
        deviceGps.add("latitudeFraction", new JsonPrimitive(dto.getLatitudeFraction()));

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("DeviceGps", deviceGps);
        HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);
//        log.info("设备： {} , GPS推送结果： {}", dto.getDeviceCode(), httpBackDTO.toString());
    }

    private void handleDeviceStatus(String data) {

        StateGridDeviceStatusDTO dto = GsonUtil.getInstance().fromJson(data, StateGridDeviceStatusDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return;

        Long lineId = 0L;
        Long towerId = 0L;

        if (device.getPole() != null) {
            towerId = device.getPole().getId();
            if (device.getPole().getCircuit() != null) {
                lineId = device.getPole().getCircuit().getId();
            }
        }

        JsonObject deviceStatus = new JsonObject();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        deviceStatus.add("supplierCode", new JsonPrimitive(hyConfigProperty.getConstant().getSupplierCode()));
        deviceStatus.add("deviceId", new JsonPrimitive(device.getId()));
        deviceStatus.add("deviceCode", new JsonPrimitive(device.getCode()));
        deviceStatus.add("lineId", new JsonPrimitive(lineId));
        deviceStatus.add("towerId", new JsonPrimitive(towerId));
        deviceStatus.add("phase", new JsonPrimitive(device.getPhase()));

        String collectTime = hexTimeToTimeString(dto.getUploadTime(), formatter);
        if (StringUtils.isEmpty(collectTime)) {
            collectTime = formatter.format(ZonedDateTime.now());
        }
        deviceStatus.add("collectionTime", new JsonPrimitive(collectTime));

        deviceStatus.add("solarChargeCurrent", new JsonPrimitive(dto.getSolarChargeCurrent()));
        deviceStatus.add("powerCollectCurrent", new JsonPrimitive(dto.getPowerCollectCurrent()));
        deviceStatus.add("workVoltage", new JsonPrimitive(dto.getWorkVoltage()));
        deviceStatus.add("workCurrent", new JsonPrimitive(dto.getWorkCurrent()));
        deviceStatus.add("batteryVoltage", new JsonPrimitive(dto.getBatteryVoltage()));
        Float batteryCharge = batteryUtils.getBatteryCharge(Float.valueOf(dto.getBatteryVoltage()) / 1000);
        deviceStatus.add("batteryCharge", new JsonPrimitive(batteryCharge));

        deviceStatus.add("fpgaPowerState", new JsonPrimitive(dto.getFpgaPowerState()));
        deviceStatus.add("solarVoltageA", new JsonPrimitive(dto.getSolarVoltageA()));
        deviceStatus.add("solarVoltageB", new JsonPrimitive(dto.getSolarVoltageB()));
        deviceStatus.add("solarVoltageC", new JsonPrimitive(dto.getSolarVoltageC()));
        deviceStatus.add("powerCollectVoltage", new JsonPrimitive(dto.getPowerCollectVoltage()));
        deviceStatus.add("chipTemperature", new JsonPrimitive(dto.getChipTemperature()));
        deviceStatus.add("mainbordTemperature", new JsonPrimitive(dto.getMainbordTemperature()));
        deviceStatus.add("signalStrength", new JsonPrimitive(dto.getSignalStrength()));

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("DeviceStatus", deviceStatus);
        HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);
//        log.info("设备： {} , 设备状态推送结果： {}", dto.getDeviceCode(), httpBackDTO.toString());
    }


    private void handleDeviceWorkHours(String data) {

        StateGridDeviceWorkHoursDTO dto = GsonUtil.getInstance().fromJson(data, StateGridDeviceWorkHoursDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return;

        Long lineId = 0L;
        Long towerId = 0L;

        if (device.getPole() != null) {
            towerId = device.getPole().getId();
            if (device.getPole().getCircuit() != null) {
                lineId = device.getPole().getCircuit().getId();
            }
        }

        JsonObject workHours = new JsonObject();

        workHours.add("supplierCode", new JsonPrimitive(hyConfigProperty.getConstant().getSupplierCode()));
        workHours.add("deviceId", new JsonPrimitive(device.getId()));
        workHours.add("deviceCode", new JsonPrimitive(device.getCode()));
        workHours.add("lineId", new JsonPrimitive(lineId));
        workHours.add("towerId", new JsonPrimitive(towerId));
        workHours.add("phase", new JsonPrimitive(device.getPhase()));

        workHours.add("continuousWorkHours", new JsonPrimitive(dto.getContinuousWorkHours()));
        workHours.add("totalWorkHours", new JsonPrimitive(dto.getTotalWorkHours()));

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("DeviceWorkHours", workHours);
        HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);
//        log.info("设备： {} , 工作状态推送结果： {}", dto.getDeviceCode(), httpBackDTO.toString());
    }

    private void handleHistoryData(String data) {

        StateGridHistoryDataDTO dto = GsonUtil.getInstance().fromJson(data, StateGridHistoryDataDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return;

        Long lineId = 0L;
        Long towerId = 0L;

        if (device.getPole() != null) {
            towerId = device.getPole().getId();
            if (device.getPole().getCircuit() != null) {
                lineId = device.getPole().getCircuit().getId();
            }
        }

        JsonObject historyData = new JsonObject();

        historyData.add("supplierCode", new JsonPrimitive(hyConfigProperty.getConstant().getSupplierCode()));
        historyData.add("deviceId", new JsonPrimitive(device.getId()));
        historyData.add("deviceCode", new JsonPrimitive(device.getCode()));
        historyData.add("type", new JsonPrimitive("historyData"));
        historyData.add("lineId", new JsonPrimitive(lineId));
        historyData.add("towerId", new JsonPrimitive(towerId));
        historyData.add("phase", new JsonPrimitive(device.getPhase()));

        historyData.add("totalNumber", new JsonPrimitive(dto.getTotalNumber()));
        historyData.add("waveNumber", new JsonPrimitive(dto.getWaveNumber()));
        historyData.add("frequencyNumber", new JsonPrimitive(dto.getFrequencyNumber()));

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("HistoryData", historyData);
        HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);
//        log.info("设备： {} , 历史数据推送结果： {}", dto.getDeviceCode(), httpBackDTO.toString());
    }

    private void handleDeviceDailyHistoryData(String data) {

        StateGridDeviceDailyHistoryDataDTO dto = GsonUtil.getInstance().fromJson(data, StateGridDeviceDailyHistoryDataDTO.class);

        Device device = deviceService.findDeviceByCode(dto.getDeviceCode());

        if (device == null) return;

        Long lineId = 0L;
        Long towerId = 0L;

        if (device.getPole() != null) {
            towerId = device.getPole().getId();
            if (device.getPole().getCircuit() != null) {
                lineId = device.getPole().getCircuit().getId();
            }
        }

        JsonObject historyData = new JsonObject();

        historyData.add("supplierCode", new JsonPrimitive(hyConfigProperty.getConstant().getSupplierCode()));
        historyData.add("deviceId", new JsonPrimitive(device.getId()));
        historyData.add("deviceCode", new JsonPrimitive(device.getCode()));
        historyData.add("lineId", new JsonPrimitive(lineId));
        historyData.add("towerId", new JsonPrimitive(towerId));
        historyData.add("phase", new JsonPrimitive(device.getPhase()));

        JsonArray historyDataArray = generateHistoryDataArray(dto);
        if (historyDataArray.size() > 0) {
            historyData.add("historyData", historyDataArray);
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("DeviceDailyHistoryData", historyData);
            HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);
//            log.info("设备： {} , 历史数据条数推送结果： {}", dto.getDeviceCode(), httpBackDTO.toString());
        }

    }

    private void handleDeviceRunStatusData(String data) {
        StateGridDeviceRunStatusDTO dto = GsonUtil.getInstance().fromJson(data, StateGridDeviceRunStatusDTO.class);

        JsonObject runStatusData = new JsonObject();

        runStatusData.add("deviceCode", new JsonPrimitive(dto.getDeviceCode()));
        runStatusData.add("packetType", new JsonPrimitive(dto.getPacketType()));
        JsonObject contentObj = new JsonObject();
        if (dto.getFlag() != null && dto.getFlag() == 0) {
            contentObj.add("flag", new JsonPrimitive(true));
        } else if (dto.getFlag() != null && dto.getFlag() == 255) {
            contentObj.add("flag", new JsonPrimitive(false));
        } else {
            contentObj.add("flag", new JsonPrimitive(false));
        }
        runStatusData.add("content", contentObj);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("DeviceRunStatusData", runStatusData);
        HttpBackDTO httpBackDTO = HttpClientUtil.execute("POST", hyConfigProperty.getAccess().getHost(), hyConfigProperty.getAccess().getPort(), hyConfigProperty.getAccess().getApi(), null, jsonObject.toString(), null, false);
//        log.info("设备： {} , 设备运行状态响应推送结果： {}", dto.getDeviceCode(), httpBackDTO.toString());
    }


    // 生成HistoryData     ==========================
    private JsonArray generateHistoryDataArray(StateGridDeviceDailyHistoryDataDTO baseCommandDTO) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject1 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime1(), null), baseCommandDTO.getWaveNum1(), baseCommandDTO.getFrequencyNum1());
        if (jsonObject1 != null) {
            jsonArray.add(jsonObject1);
        }
        JsonObject jsonObject2 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime2(), null), baseCommandDTO.getWaveNum2(), baseCommandDTO.getFrequencyNum2());
        if (jsonObject2 != null) {
            jsonArray.add(jsonObject2);
        }
        JsonObject jsonObject3 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime3(), null), baseCommandDTO.getWaveNum3(), baseCommandDTO.getFrequencyNum3());
        if (jsonObject3 != null) {
            jsonArray.add(jsonObject3);
        }
        JsonObject jsonObject4 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime4(), null), baseCommandDTO.getWaveNum4(), baseCommandDTO.getFrequencyNum4());
        if (jsonObject4 != null) {
            jsonArray.add(jsonObject4);
        }
        JsonObject jsonObject5 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime5(), null), baseCommandDTO.getWaveNum5(), baseCommandDTO.getFrequencyNum5());
        if (jsonObject5 != null) {
            jsonArray.add(jsonObject5);
        }
        JsonObject jsonObject6 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime6(), null), baseCommandDTO.getWaveNum6(), baseCommandDTO.getFrequencyNum6());
        if (jsonObject6 != null) {
            jsonArray.add(jsonObject6);
        }
        JsonObject jsonObject7 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime7(), null), baseCommandDTO.getWaveNum7(), baseCommandDTO.getFrequencyNum7());
        if (jsonObject7 != null) {
            jsonArray.add(jsonObject7);
        }
        JsonObject jsonObject8 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime8(), null), baseCommandDTO.getWaveNum8(), baseCommandDTO.getFrequencyNum8());
        if (jsonObject8 != null) {
            jsonArray.add(jsonObject8);
        }
        JsonObject jsonObject9 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime9(), null), baseCommandDTO.getWaveNum9(), baseCommandDTO.getFrequencyNum9());
        if (jsonObject9 != null) {
            jsonArray.add(jsonObject9);
        }
        JsonObject jsonObject10 = generateHistoryDataObject(hexTimeToTimeString(baseCommandDTO.getCollectionTime10(), null), baseCommandDTO.getWaveNum10(), baseCommandDTO.getFrequencyNum10());
        if (jsonObject10 != null) {
            jsonArray.add(jsonObject10);
        }
        return jsonArray;
    }

    private JsonObject generateHistoryDataObject(String collectionTime, Integer waveNum, Integer frequencyNum) {
        JsonObject jsonObject = null;

        if (StringUtils.isEmpty(collectionTime)) return jsonObject;

        try {
            jsonObject = new JsonObject();
            jsonObject.add("collectionTime", new JsonPrimitive(collectionTime));
            jsonObject.add("waveNum", new JsonPrimitive(waveNum));
            jsonObject.add("frequencyNum", new JsonPrimitive(frequencyNum));
        } catch (Exception e) {
            log.error("封装HistoryDataObject出现错误", e);
        }
        return jsonObject;
    }

}
