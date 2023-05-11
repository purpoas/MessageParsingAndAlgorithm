package com.hy.biz.dataResolver.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.hy.biz.dataResolver.parser.SubscribedMessageParser;
import com.hy.biz.dataResolver.entity.dto.MessageDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver
 * @description
 * @create 2023-05-10 14:38
 **/
@Component
public class CtrlMsgHandler implements MessageHandler {
    private final SubscribedMessageParser subscribedMessageParser;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper;

    public CtrlMsgHandler(SubscribedMessageParser subscribedMessageParser, RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        this.subscribedMessageParser = subscribedMessageParser;
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }

    @Override
    public boolean canHandle(JsonNode message) {
        return message.has("data");
    }

    @Override
    public void handle(JsonNode message, String channel) {
        MessageDTO messageDTO = mapper.convertValue(message, MessageDTO.class);
        JsonObject jsonObject = subscribedMessageParser.parseCtrlMsg(
                messageDTO.getData().getCommand(),
                messageDTO.getDeviceCode());
        redisTemplate.convertAndSend(channel, jsonObject.toString());
    }

}

