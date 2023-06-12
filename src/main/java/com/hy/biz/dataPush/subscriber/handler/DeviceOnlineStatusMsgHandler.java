package com.hy.biz.dataPush.subscriber.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.hy.biz.dataParsing.exception.MessageParsingException;
import com.hy.biz.dataParsing.parser.ParserHelper;
import com.hy.biz.dataParsing.parser.SubscribedMessageParser;
import com.hy.biz.dataParsing.dto.DeviceOnlineStatusDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.hy.biz.dataParsing.constants.MessageConstants.*;

/**
 * 设备上下线报文处理器
 *
 * @author shiwentao
 * @package com.hy.biz.dataParsing
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
    public boolean canHandle(@NonNull JsonNode message) {
        return message.has("result");
    }

    @Override
    public void handle(@NonNull JsonNode message, @NonNull String channel) {
        DeviceOnlineStatusDTO deviceOnlineStatusDTO = convertToDTO(message);

        //设备上下线入库 & 返回按照协议解析好的设备上下线 JSON 数据
        JsonObject jsonObject = parseDTO(deviceOnlineStatusDTO);

        //将设备上下线的 JSON 数据发布到 REDIS 订阅频道
        redisTemplate.convertAndSend(channel, jsonObject.toString());

        // 维护更新 REDIS 频道 hydnm:cache:status 中的设备最新信息
        parserHelper.maintainDeviceStatusInRedis(jsonObject);
    }


    // ===============private===============private===============private===============private===============private============


    private DeviceOnlineStatusDTO convertToDTO(@NonNull JsonNode message) {
        try {
            return mapper.convertValue(message, DeviceOnlineStatusDTO.class);
        } catch (IllegalArgumentException e) {
            throw new MessageParsingException(MESSAGE_TO_DEVICE_ONLINE_STATUS_DTO_ERROR, e);
        }
    }

    private JsonObject parseDTO(DeviceOnlineStatusDTO deviceOnlineStatusDTO) {
        try {
            return subscribedMessageParser.parseDeviceOnlineStatDTO(deviceOnlineStatusDTO);
        } catch (Exception e) {
            throw new MessageParsingException(DTO_TO_JSON_ERROR, e);
        }
    }


}
