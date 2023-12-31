package com.hy.biz.dataParsing.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.hy.biz.dataParsing.dto.MessageDTO;
import com.hy.biz.dataParsing.dto.WaveDataMessage;
import com.hy.biz.dataParsing.exception.MessageParsingException;
import com.hy.biz.dataParsing.util.DateTimeUtil;
import com.hy.config.HyConfigProperty;
import com.hy.domain.DeviceInfo;
import com.hy.domain.DeviceStatus;
import com.hy.domain.WaveData;
import com.hy.domain.WorkStatus;
import com.hy.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.hy.biz.dataParsing.constants.FrameType.MONITORING_DATA_REPORT;
import static com.hy.biz.dataParsing.constants.MessageConstants.*;
import static com.hy.biz.dataParsing.constants.MessageType.*;

/**
 *===================
 * 解析器 Helper 类   ｜
 *===================
 *
 * @author shiwentao
 * @package com.hy.biz.dataParsing.algorithmUtil
 * @create 2023-05-06 10:57
 **/
@Slf4j
@Component
public class ParserHelper {

    private final HyConfigProperty hyConfigProperty;
    private final DeviceRepository deviceRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper;

    public ParserHelper(HyConfigProperty hyConfigProperty, DeviceRepository deviceRepository, RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        this.hyConfigProperty = hyConfigProperty;
        this.deviceRepository = deviceRepository;
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }

    public MessageDTO createMessageDTO(String commandData) {

        MessageDTO messageDTO;
        try {
            messageDTO = mapper.readValue(commandData, MessageDTO.class);
        } catch (JsonProcessingException e) {
            throw new MessageParsingException(JSON_TO_DTO_ERROR);
        }

        return messageDTO;
    }

    public void checkHeader(ByteBuffer buffer) {
        if (buffer.getShort() != HEADER) throw new IllegalArgumentException(ILLEGAL_HEADER_ERROR);
    }

    public byte parseFrameType(ByteBuffer buffer) {
        buffer.position(buffer.position() + ID_LENGTH);
        return buffer.get();
    }

    public byte parseMessageType(ByteBuffer buffer) {
        return buffer.get();
    }

    public byte[] parseMessageContent(ByteBuffer buffer) {
        byte[] messageContent = new byte[buffer.getShort()];
        buffer.get(messageContent);
        return messageContent;
    }

    public void checkSum(ByteBuffer buffer) {
        buffer.position(buffer.position() + CHECK_SUM_LENGTH);
    }

    public void publishToRedis(byte frameType, byte messageType, long timeStamp, String deviceCode, JsonObject params) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", true);
        String msg = createMsg(frameType, messageType);
        jsonObject.addProperty("msg", msg);
        jsonObject.addProperty("msgType", String.format("0x%02X:0x%02X", frameType, messageType));
        jsonObject.addProperty("timestamp", Instant.ofEpochMilli(timeStamp).getEpochSecond());
        jsonObject.addProperty("deviceCode", deviceCode);
        jsonObject.add("param", params);

        log.info("向频道 {} 中推送消息 {}", hyConfigProperty.getDataQueue().getDnmTopicChannelPb(), jsonObject);
        redisTemplate.convertAndSend(hyConfigProperty.getDataQueue().getDnmTopicChannelPb(), jsonObject.toString());
    }

    public void maintainDeviceStatusInRedis(DeviceStatus deviceStatus, String deviceCode) {
        String dnmLatestDeviceStatus = hyConfigProperty.getConstant().getDnmLatestDeviceStatus();

        String batteryVoltage = String.valueOf(deviceStatus.getBatteryVoltage());
        Map<String, String> map = new HashMap<>();
        map.put("batteryVoltage", batteryVoltage);
        map.put("updateTime", String.valueOf(Instant.now().getEpochSecond()));

        redisTemplate.opsForHash().putAll(dnmLatestDeviceStatus + ":" + deviceCode, map);
    }

    public void maintainDeviceStatusInRedis(DeviceInfo deviceInfo, String deviceCode) {
        String dnmLatestDeviceStatus = hyConfigProperty.getConstant().getDnmLatestDeviceStatus();

        Map<String, String> map = new HashMap<>();
        map.put("lastTime", String.valueOf(deviceInfo.getCollectionTime().getEpochSecond()));
        map.put("updateTime", String.valueOf(Instant.now().getEpochSecond()));

        redisTemplate.opsForHash().putAll(dnmLatestDeviceStatus + ":" + deviceCode, map);
    }

    public void maintainDeviceStatusInRedis(WorkStatus workStatus, String deviceCode) {
        String dnmLatestDeviceStatus = hyConfigProperty.getConstant().getDnmLatestDeviceStatus();

        Map<String, String> map = new HashMap<>();
        map.put("workCurrent", String.valueOf(workStatus.getLineCurrent()));
        map.put("updateTime", String.valueOf(Instant.now().getEpochSecond()));

        redisTemplate.opsForHash().putAll(dnmLatestDeviceStatus + ":" + deviceCode, map);
    }

    public void maintainDeviceStatusInRedis(JsonObject jsonObject) {
        String dnmLatestDeviceStatus = hyConfigProperty.getConstant().getDnmLatestDeviceStatus();

        Map<String, String> map = new HashMap<>();
        if (jsonObject.has("status")) {
            if (jsonObject.get("status").getAsString().equals("true"))
                map.put("onlineTime", String.valueOf(Instant.ofEpochMilli(jsonObject.get("timestamp").getAsLong()).getEpochSecond()));
            if (jsonObject.get("status").getAsString().equals("false"))
                map.put("offlineTime", String.valueOf(Instant.ofEpochMilli(jsonObject.get("timestamp").getAsLong()).getEpochSecond()));
            map.put("updateTime", String.valueOf(Instant.now().getEpochSecond()));
        }

        if (jsonObject.has("deviceCode"))
            redisTemplate.opsForHash().putAll(dnmLatestDeviceStatus + ":" + jsonObject.get("deviceCode").getAsString(), map);
    }

    public JsonObject createJsonMsg(ByteBuffer buffer, String messageSignature, String deviceCode, long timeStamp, String operationName) {
        JsonObject jsonObject = new JsonObject();
        byte isSuccessful = buffer.get();
        String operationResult;
        boolean status;

        if (isSuccessful == MESSAGE_STATUS_SUCCESSFUL) {
            operationResult = OPERATION_SUCCESSFUL;
            status = true;
        } else if (isSuccessful == MESSAGE_STATUS_FAILED) {
            operationResult = OPERATION_FAILED;
            status = false;
        } else {
            operationResult = UNKNOWN_OPERATION_RESULT;
            status = false;
        }

        jsonObject.addProperty("status", status);
        jsonObject.addProperty("msg", String.format("%s：%s", operationName, operationResult));
        jsonObject.addProperty("msgType", messageSignature);
        jsonObject.addProperty("timestamp", timeStamp / 1000);
        jsonObject.addProperty("deviceCode", deviceCode);

        return jsonObject;
    }

    public WaveData setWaveDataProperty(WaveDataMessage message, long timeStamp, String deviceCode) {
        WaveData waveData = new WaveData();

        waveData.setCollectionTime(Instant.ofEpochMilli(timeStamp));
        waveData.setDeviceId(deviceRepository.findDeviceIdByCode(deviceCode));

        Map<Integer, Integer> waveTypeMap = new HashMap<>();
        waveTypeMap.put(1, 1);
        waveTypeMap.put(3, 2);
        waveTypeMap.put(5, 3);
        int waveType = waveTypeMap.getOrDefault((int) message.getMessageType(), 0);
        waveData.setType(waveType);

        waveData.setCode(generateWaveDataCode(timeStamp, message.getFrameType(), message.getMessageType(), deviceCode));
        waveData.setLength((long) message.getWaveDataLength());
        waveData.setHeadTime(message.getWaveStartTime());
        waveData.setSampleRate(hyConfigProperty.getConstant().getTravelSampleRate());
        waveData.setThreshold(hyConfigProperty.getConstant().getTravelMaxThreshold());
        waveData.setRelaFlag(parseRelaFalg());
        waveData.setData(message.getWaveData());
        waveData.setRemark(message.getReserved());

        return waveData;
    }

    public void publishWaveData(WaveData waveData, WaveDataMessage message, long timeStamp, String deviceCode) {
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

        publishToRedis(message.getFrameType(), message.getMessageType(), timeStamp, deviceCode, params);
    }


    // 私有方法==========================================================================================================


    private String createMsg(byte frameType, byte messageType) {
        if (frameType == MONITORING_DATA_REPORT && messageType == TRAVELLING_WAVE_CURRENT) {
            return "行波电流";
        }
        if (frameType == MONITORING_DATA_REPORT && messageType == FAULT_CURRENT) {
            return "故障电流";
        }
        if (frameType == MONITORING_DATA_REPORT && messageType == FAULT_VOLTAGE) {
            return "故障电压";
        }

        return "";
    }

    private String generateWaveDataCode(long timeStamp, byte frameType, byte messageType, String deviceCode) {
        byte[] dateTimeBytes = DateTimeUtil.parseLongDateToBytes(timeStamp);
        String ymd = String.format("20%02d%02d%02d", dateTimeBytes[0] & 0xFF, dateTimeBytes[1] & 0xFF, dateTimeBytes[2] & 0xFF);
        String hms = String.format("%02d%02d%02d", dateTimeBytes[3] & 0xFF, dateTimeBytes[4] & 0xFF, dateTimeBytes[5] & 0xFF);
        String ns = String.format("%03d%03d%03d",
                ((dateTimeBytes[6] & 0xFF) << 8) + (dateTimeBytes[7] & 0xFF),
                ((dateTimeBytes[8] & 0xFF) << 8) + (dateTimeBytes[9] & 0xFF),
                ((dateTimeBytes[10] & 0xFF) << 8) + (dateTimeBytes[11] & 0xFF)
        );

        String frameTypeStr = String.format("%02d", frameType);
        String messageTypeStr = String.format("%02d", messageType);

        return String.format("W%s-%s-%s-%s%s-%s", ymd, hms, ns, frameTypeStr, messageTypeStr, deviceCode);
    }

    //todo 需结合算法
    private Integer parseRelaFalg() {
        return 1;
    }


}
