package com.hy.biz.parser;

import com.google.gson.JsonObject;
import com.hy.biz.parser.entity.dto.DeviceOnlineStatusDTO;
import com.hy.biz.parser.exception.MessageParsingException;
import com.hy.domain.DeviceOnlineStatus;
import com.hy.repository.DeviceOnlineStatusRepository;
import com.hy.repository.DeviceRepository;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.Instant;

import static com.hy.biz.parser.constants.FrameType.CONTROL_ACK_REPORT;
import static com.hy.biz.parser.constants.MessageConstants.*;
import static com.hy.biz.parser.constants.MessageType.PARAMETER_READING;
import static com.hy.biz.parser.util.TypeConverter.hexStringToByteArray;
import static java.nio.ByteOrder.BIG_ENDIAN;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.subscriber
 * @description 解析器：用于解析 Redis 订阅频道中的控制报文
 * @create 2023-05-08 16:12
 **/
@Component
public class SubscribedMessageParser {
    private final DeviceRepository deviceRepository;
    private final DeviceOnlineStatusRepository deviceOnlineStatusRepository;

    public SubscribedMessageParser(DeviceRepository deviceRepository, DeviceOnlineStatusRepository deviceOnlineStatusRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceOnlineStatusRepository = deviceOnlineStatusRepository;
    }

    public JsonObject parseCtrlMsg(String commandData, long timeStamp, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(hexStringToByteArray(commandData)).order(BIG_ENDIAN);

        if (buffer.getShort() != HEADER) {
            throw new IllegalArgumentException(ILLEGAL_HEADER_ERROR);
        }

        buffer.get(new byte[ID_LENGTH]);
        byte frameType = buffer.get();
        byte messageType = buffer.get();
        byte[] messageContent = new byte[buffer.getShort()];
        buffer.get(messageContent);

        return parseMessageContent(messageContent, frameType, messageType, timeStamp, deviceCode);
    }

    public void parseDeviceOnlineStatMsg(DeviceOnlineStatusDTO deviceOnlineStatusDTO) {
        DeviceOnlineStatus deviceOnlineStatus = new DeviceOnlineStatus();

        String[] msg = deviceOnlineStatusDTO.getResult().getMsg().split(" ");
        deviceOnlineStatus.setDeviceId(deviceRepository.findDeviceIdByCode(msg[0]));
        deviceOnlineStatus.setCollectionTime(Instant.now());
        deviceOnlineStatus.setMessage(deviceOnlineStatusDTO.getResult().getMsg());
        deviceOnlineStatus.setStatus(deviceOnlineStatusDTO.getResult().getStatus());

        deviceOnlineStatusRepository.save(deviceOnlineStatus);
    }

    private JsonObject parseMessageContent(byte[] messageContent, byte frameType, byte messageType, long timeStamp, String deviceCode) {
        if (frameType == CONTROL_ACK_REPORT && messageType == PARAMETER_READING) {
            return parseParamReadingMsg(messageContent, timeStamp, deviceCode);
        }
        //add more ctrl message parsing method...
        return null;
    }

    private JsonObject parseParamReadingMsg(byte[] messageContent, long timeStamp, String deviceCode) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);
        byte stat = buffer.get();
        String status;

        switch (stat) {
            case MESSAGE_STATUS_SUCCESS:
                status = "success";
                break;
            case MESSAGE_STATUS_FAILURE:
                status = "failure";
                break;
            default:
                throw new MessageParsingException("Invalid message status: " + stat);
        }

        JsonObject paramReadingMsg = new JsonObject();
        paramReadingMsg.addProperty("status", status);
        paramReadingMsg.addProperty("timeStamp", Instant.ofEpochMilli(timeStamp).toString());
        paramReadingMsg.addProperty("deviceId", Long.toString(deviceRepository.findDeviceIdByCode(deviceCode)));

        short paramNum = buffer.getShort();
        JsonObject params = new JsonObject();

        for (int i = 0; i < paramNum; i++) {
            short paramId = buffer.getShort();
            int paramContent = buffer.getInt();

            String paramIdStr = String.format("param%d", paramId);
            String paramContentStr = Integer.toString(paramContent);

            params.addProperty(paramIdStr, paramContentStr);
        }

        paramReadingMsg.add("param", params);

        return paramReadingMsg;
    }

}
