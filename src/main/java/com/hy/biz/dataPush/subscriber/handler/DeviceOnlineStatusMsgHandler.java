package com.hy.biz.dataPush.subscriber.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.hy.biz.dataParsing.parser.ParserHelper;
import com.hy.biz.dataParsing.parser.SubscribedMessageParser;
import com.hy.biz.dataParsing.dto.DeviceOnlineStatusDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing
 * @description
 * @create 2023-05-10 14:44
 **/
@Component
@Slf4j
public class DeviceOnlineStatusMsgHandler implements MessageHandler {

    private final ParserHelper parserHelper;
    private final RedisTemplate<String, String> redisTemplate;
    private final SubscribedMessageParser subscribedMessageParser;
    private final ObjectMapper mapper;

    public DeviceOnlineStatusMsgHandler(ParserHelper parserHelper, RedisTemplate<String, String> redisTemplate, SubscribedMessageParser subscribedMessageParser, ObjectMapper mapper) {
        this.parserHelper = parserHelper;
        this.redisTemplate = redisTemplate;
        this.subscribedMessageParser = subscribedMessageParser;
        this.mapper = mapper;
    }

    @Override
    public boolean canHandle(JsonNode message) {
        return message.has("result");
    }

    @Override
    public void handle(JsonNode message, String channel) {

        DeviceOnlineStatusDTO deviceOnlineStatusDTO = mapper.convertValue(message, DeviceOnlineStatusDTO.class);
        JsonObject jsonObject = subscribedMessageParser.parseDeviceOnlineStatMsg(deviceOnlineStatusDTO);
        redisTemplate.convertAndSend(channel, jsonObject.toString());
        parserHelper.maintainDeviceStatus(jsonObject);

    }


}
