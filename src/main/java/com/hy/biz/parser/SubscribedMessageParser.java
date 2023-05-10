package com.hy.biz.parser;

import com.google.gson.JsonObject;
import com.hy.biz.parser.entity.dto.DeviceOnlineStatusDTO;
import com.hy.biz.parser.exception.MessageParsingException;
import com.hy.biz.parser.registry.ParamCodeRegistry;
import com.hy.domain.DeviceOnlineStatus;
import com.hy.repository.DeviceOnlineStatusRepository;
import com.hy.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.hy.biz.parser.constants.MessageConstants.*;
import static com.hy.biz.parser.util.TypeConverter.hexStringToByteArray;
import static java.nio.ByteOrder.BIG_ENDIAN;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.subscriber
 * @description 解析器：用于解析 Redis 订阅频道中的控制报文，及设备上线通知报文
 * @create 2023-05-08 16:12
 **/
@Component
@Slf4j
public class SubscribedMessageParser {
    private final Map<String, String> PARAM_CODE_MAP = ParamCodeRegistry.getParamCodeMap();

    private final DeviceRepository deviceRepository;
    private final DeviceOnlineStatusRepository deviceOnlineStatusRepository;

    public SubscribedMessageParser(DeviceRepository deviceRepository, DeviceOnlineStatusRepository deviceOnlineStatusRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceOnlineStatusRepository = deviceOnlineStatusRepository;
    }

    public JsonObject parseCtrlMsg(String commandData, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(hexStringToByteArray(commandData)).order(BIG_ENDIAN);
        if (buffer.getShort() != HEADER)
            throw new IllegalArgumentException(ILLEGAL_HEADER_ERROR);
        buffer.position(buffer.position() + ID_LENGTH);
        byte frameType = buffer.get();
        byte messageType = buffer.get();
        byte[] messageContent = new byte[buffer.getShort()];
        buffer.get(messageContent);

        return parseMessageContent(messageContent, frameType, messageType, deviceCode);
    }

    public void parseDeviceOnlineStatMsg(DeviceOnlineStatusDTO deviceOnlineStatusDTO) {
        DeviceOnlineStatus deviceOnlineStatus = deviceOnlineStatusDTO.transform(deviceRepository);
        deviceOnlineStatusRepository.save(deviceOnlineStatus);
    }

    private JsonObject parseMessageContent(byte[] messageContent, byte frameType, byte messageType, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);
        String messageSignature = String.format("0x%02X:0x%02X", frameType, messageType);

        switch (messageSignature) {
            case "0x04:0x01":
                return parseDeviceResetRspMsg(buffer, messageSignature, deviceCode);
            case "0x04:0x03":
                return parseParamSettingRspMsg(buffer, messageSignature, deviceCode);
            case "0x04:0x05":
                return parseParamReadingRspMsg(buffer, messageSignature, deviceCode);
            case "0x04:0x07":
                return parseProgramUpgradeRspMsg(buffer, messageSignature, deviceCode);
            case "0x04:0x015":
            case "0x05:0x015":
                return parseDeviceHistoricalDataRspMsg(buffer, messageSignature, deviceCode);
            default:
                log.error("未知报文签名: {}，无法识别具体控制数据报文类型，设备编号为: {}", messageSignature, deviceCode);
                throw new MessageParsingException(ILLEGAL_MESSAGE_SIGNATURE_ERROR);
        }
    }

    private JsonObject parseDeviceResetRspMsg(ByteBuffer buffer, String messageSignature, String deviceCode) {
        String operationSucceeded = operationSucceeded(buffer);
        JsonObject deviceResetRspMsg = new JsonObject();
        deviceResetRspMsg.addProperty("status", "装置复位" + operationSucceeded);
        deviceResetRspMsg.addProperty("msgType", messageSignature);
        deviceResetRspMsg.addProperty("deviceCode", deviceCode);

        return deviceResetRspMsg;
    }

    private JsonObject parseParamSettingRspMsg(ByteBuffer buffer, String messageSignature, String deviceCode) {
        String operationSucceeded = operationSucceeded(buffer);
        JsonObject paramSettingRspMsg = new JsonObject();
        paramSettingRspMsg.addProperty("status", "参数设置" + operationSucceeded);
        paramSettingRspMsg.addProperty("msgType", messageSignature);
        paramSettingRspMsg.addProperty("deviceCode", deviceCode);

        return paramSettingRspMsg;
    }

    private JsonObject parseParamReadingRspMsg(ByteBuffer buffer, String messageSignature, String deviceCode) {
        String operationSucceeded = operationSucceeded(buffer);
        JsonObject paramReadingRspMsg = new JsonObject();
        paramReadingRspMsg.addProperty("status", operationSucceeded);
        paramReadingRspMsg.addProperty("msgType", messageSignature);
        paramReadingRspMsg.addProperty("deviceCode", deviceCode);

        JsonObject params = new JsonObject();
        for (int i = 0, paramNum = buffer.getShort(); i < paramNum; i++) {
            String paramCodeHex = String.format("0x%04x", buffer.getShort() & 0xffff);
            params.addProperty(PARAM_CODE_MAP.get(paramCodeHex), Integer.toString(buffer.getInt()));
        }
        paramReadingRspMsg.add("param", params);

        return paramReadingRspMsg;
    }

    private JsonObject parseProgramUpgradeRspMsg(ByteBuffer buffer, String messageSignature, String deviceCode) {
        String operationSucceeded = operationSucceeded(buffer);
        JsonObject programUpgradeRspMsg = new JsonObject();
        programUpgradeRspMsg.addProperty("status", operationSucceeded);
        programUpgradeRspMsg.addProperty("msgType", messageSignature);
        programUpgradeRspMsg.addProperty("deviceCode", deviceCode);

        return programUpgradeRspMsg;
    }

    private JsonObject parseDeviceHistoricalDataRspMsg(ByteBuffer buffer, String messageSignature, String deviceCode) {
        String operationSucceeded = operationSucceeded(buffer);
        JsonObject deviceHistoricalDataRspMsg = new JsonObject();
        deviceHistoricalDataRspMsg.addProperty("status", operationSucceeded);
        deviceHistoricalDataRspMsg.addProperty("msgType", messageSignature);
        deviceHistoricalDataRspMsg.addProperty("deviceCode", deviceCode);

        JsonObject params = new JsonObject();
        params.addProperty("historicalDataNum", buffer.getShort());
        params.addProperty("historicalTravellingWaveCurrentDataNum", buffer.getShort());
        params.addProperty("historicalPowerFrequencyCurrentDataNum", buffer.getShort());
        params.addProperty("historicalElectricalFieldVoltageDataNum", buffer.getShort());

        deviceHistoricalDataRspMsg.add("param", params);

        return deviceHistoricalDataRspMsg;
    }

    private String operationSucceeded(ByteBuffer buffer) {
        byte isSuccessful = buffer.get();

        String status;
        switch (isSuccessful) {
            case MESSAGE_STATUS_SUCCESSFUL:
                status = "操作成功";
                break;
            case MESSAGE_STATUS_FAILED:
                status = "操作失败";
                break;
            default:
                throw new MessageParsingException(String.format("%s: %d", UNKNOWN_OPERATION_RESULT, isSuccessful));
        }

        return status;
    }

}
