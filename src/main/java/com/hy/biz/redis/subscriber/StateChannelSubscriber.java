package com.hy.biz.redis.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.hy.biz.parser.SubscribedMessageParser;
import com.hy.biz.parser.entity.dto.DeviceOnlineStatusDTO;
import com.hy.biz.parser.entity.dto.MessageDTO;
import com.hy.biz.parser.exception.MessageParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;

import static com.hy.biz.parser.util.TypeConverter.byteArrToStr;

/**
 * @author shiwentao
 * @package com.hy.config
 * @description
 * @create 2023-05-04 16:07
 **/
@Slf4j
public class StateChannelSubscriber implements MessageListener {
    private final SubscribedMessageParser subscribedMessageParser;
    private final ObjectMapper mapper;

    public StateChannelSubscriber(SubscribedMessageParser subscribedMessageParser, ObjectMapper mapper) {
        this.subscribedMessageParser = subscribedMessageParser;
        this.mapper = mapper;
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        JsonNode rootNode;
        try {
            log.info("Redis订阅频道收到的消息（用于调试）: {}", message);
            rootNode = mapper.readTree(byteArrToStr(message.getBody()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (rootNode.has("data")) { //控制报文
            MessageDTO messageDTO = mapper.convertValue(rootNode, MessageDTO.class);
            JsonObject jsonObject = subscribedMessageParser.parseCtrlMsg(
                    messageDTO.getData().getCommand(),
                    messageDTO.getTimeStamp(),
                    messageDTO.getDeviceCode());

            return;
        }

        if (rootNode.has("result")) { //设备在线状态
            DeviceOnlineStatusDTO deviceOnlineStatusDTO = mapper.convertValue(rootNode, DeviceOnlineStatusDTO.class);
            subscribedMessageParser.parseDeviceOnlineStatMsg(deviceOnlineStatusDTO);
            return;
        }

        throw new MessageParsingException("无法识别订阅频道收到的消息类型（目前只支持解析设备上线通知，及控制报文）");
    }

}
