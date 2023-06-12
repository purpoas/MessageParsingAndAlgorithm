package com.hy.biz.dataPush.subscriber.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.hy.biz.dataParsing.parser.SubscribedMessageParser;
import com.hy.biz.dataParsing.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing
 * @description
 * @create 2023-05-10 14:38
 **/
@Component
@Slf4j
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
    public void  handle(JsonNode message, String channel) {
        MessageDTO messageDTO = mapper.convertValue(message, MessageDTO.class);
        JsonObject jsonObject = subscribedMessageParser.parseCtrlMsg(
                messageDTO.getData().getCommand(),
                messageDTO.getDeviceCode(),
                messageDTO.getTimeStamp()
                );
        log.info("发布到订阅频道的json数据: {}", jsonObject.toString());
        redisTemplate.convertAndSend(channel, jsonObject.toString());
    }


}

