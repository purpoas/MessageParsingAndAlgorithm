package com.hy.biz.dataParsing.parser;

import com.google.gson.JsonObject;
import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataParsing.dto.DeviceOnlineStatusDTO;
import com.hy.biz.dataParsing.exception.MessageParsingException;
import com.hy.biz.dataParsing.parser.strategy.CtrlMsgParserStrategy;
import com.hy.biz.dataParsing.registry.CtrlMsgStrategyRegistry;
import com.hy.domain.DeviceOnlineStatus;
import com.hy.repository.DeviceOnlineStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.hy.biz.dataParsing.constants.MessageConstants.ILLEGAL_MESSAGE_SIGNATURE_ERROR;
import static com.hy.biz.dataParsing.util.TypeConverter.hexStringToByteArray;
import static java.nio.ByteOrder.BIG_ENDIAN;

/**
 *
 * 解析器：用于解析 Redis 订阅频道中的控制报文，及设备上线通知报文
 *
 * @author shiwentao
 * @package com.hy.biz.redis.subscriber
 * @create 2023-05-08 16:12
 **/
@Component
@Slf4j
public class SubscribedMessageParser {

    private static final String ONLINE_STATUS_MESSAGE = "设备上线";
    private static final String OFFLINE_STATUS_MESSAGE = "设备下线";
    private static final String ONLINE_STATUS = "online";
    private static final String MSG_TYPE = "0x04:0x08";
    private final Map<String, CtrlMsgParserStrategy> strategies = CtrlMsgStrategyRegistry.getMessageStrategyMap();

    private final DataPushService dataPushService;
    private final ParserHelper parserHelper;
    private final DeviceOnlineStatusRepository deviceOnlineStatusRepository;

    @Autowired
    public SubscribedMessageParser(DataPushService dataPushService, ParserHelper parserHelper, DeviceOnlineStatusRepository deviceOnlineStatusRepository) {
        this.dataPushService = dataPushService;
        this.parserHelper = parserHelper;
        this.deviceOnlineStatusRepository = deviceOnlineStatusRepository;
    }

    /**
     *
     * 解析控制报文
     *
     * @param commandData 报文 byte 数组
     * @param deviceCode 设备编号
     * @param timeStamp 时间戳
     * @return 将发布到订阅频道的 json 数据
     */
    public JsonObject parseCtrlMsg(String commandData, String deviceCode, long timeStamp) {
        ByteBuffer buffer = ByteBuffer.wrap(hexStringToByteArray(commandData)).order(BIG_ENDIAN);
        parserHelper.checkHeader(buffer);
        byte frameType = parserHelper.parseFrameType(buffer);
        byte messageType = parserHelper.parseMessageType(buffer);
        byte[] messageContent = parserHelper.parseMessageContent(buffer);

        return parseMessageContent(messageContent, frameType, messageType, deviceCode, timeStamp);
    }

    /**
     *
     * 解析设备上下线状态报文
     *
     * @param deviceOnlineStatusDTO 设备上下线状态实体类
     * @return 将发布到订阅频道的 json 数据
     */
    public JsonObject parseDeviceOnlineStatMsg(DeviceOnlineStatusDTO deviceOnlineStatusDTO) {
        DeviceOnlineStatus deviceOnlineStatus = deviceOnlineStatusDTO.transform(dataPushService);
        deviceOnlineStatusRepository.save(deviceOnlineStatus);

        return createDeviceOnlineStatJsonMsg(deviceOnlineStatusDTO);
    }

    //======================private=================================private============================private======================

    private JsonObject parseMessageContent(byte[] messageContent, byte frameType, byte messageType, String deviceCode, long timeStamp) {
        ByteBuffer buffer = ByteBuffer.wrap(messageContent).order(BIG_ENDIAN);
        String messageSignature = String.format("0x%02X:0x%02X", frameType, messageType);

        CtrlMsgParserStrategy strategy = strategies.get(messageSignature);
        if (strategy == null) {
            log.error("未知报文签名: {}，无法识别具体控制数据报文类型", messageSignature);
            throw new MessageParsingException(ILLEGAL_MESSAGE_SIGNATURE_ERROR);
        }

        return strategy.parse(buffer, parserHelper, messageSignature, deviceCode, timeStamp);
    }

    private JsonObject createDeviceOnlineStatJsonMsg(DeviceOnlineStatusDTO deviceOnlineStatusDTO) {
        JsonObject jsonObject = new JsonObject();

        String status = deviceOnlineStatusDTO.getResult().getStatus();
        boolean isOnline = ONLINE_STATUS.equals(status);

        jsonObject.addProperty("status", isOnline);
        jsonObject.addProperty("msg", isOnline ? ONLINE_STATUS_MESSAGE : OFFLINE_STATUS_MESSAGE);
        jsonObject.addProperty("msgType", MSG_TYPE);
        jsonObject.addProperty("timestamp", deviceOnlineStatusDTO.getHeader().getTimeStamp());
        jsonObject.addProperty("deviceCode", deviceOnlineStatusDTO.getResult().getMsg().split(" ")[0]);

        return jsonObject;
    }


}
