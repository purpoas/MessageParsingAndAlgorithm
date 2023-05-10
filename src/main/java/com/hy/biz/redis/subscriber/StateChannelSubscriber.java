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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;

import static com.hy.biz.parser.constants.MessageConstants.ILLEGAL_SUBSCRIBED_MESSAGE_SIGNATURE_ERROR;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper;

    public StateChannelSubscriber(SubscribedMessageParser subscribedMessageParser, RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        this.subscribedMessageParser = subscribedMessageParser;
        this.redisTemplate = redisTemplate;
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

        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);

        if (rootNode.has("data")) { //控制报文
            MessageDTO messageDTO = mapper.convertValue(rootNode, MessageDTO.class);
            JsonObject jsonObject = subscribedMessageParser.parseCtrlMsg(
                    messageDTO.getData().getCommand(),
                    messageDTO.getDeviceCode());
            redisTemplate.convertAndSend(channel, jsonObject.toString());

            return;
        }

        if (rootNode.has("result")) { //设备在线状态
            DeviceOnlineStatusDTO deviceOnlineStatusDTO = mapper.convertValue(rootNode, DeviceOnlineStatusDTO.class);
            subscribedMessageParser.parseDeviceOnlineStatMsg(deviceOnlineStatusDTO);
            return;
        }

        throw new MessageParsingException(ILLEGAL_SUBSCRIBED_MESSAGE_SIGNATURE_ERROR);
    }

}
