package com.hy.biz.parser.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.parser.SubscribedMessageParser;
import com.hy.biz.parser.entity.dto.DeviceOnlineStatusDTO;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy.biz.parser
 * @description
 * @create 2023-05-10 14:44
 **/
@Component
public class DeviceOnlineStatusMsgHandler implements MessageHandler {
    private final SubscribedMessageParser subscribedMessageParser;
    private final ObjectMapper mapper;

    public DeviceOnlineStatusMsgHandler(SubscribedMessageParser subscribedMessageParser, ObjectMapper mapper) {
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
        subscribedMessageParser.parseDeviceOnlineStatMsg(deviceOnlineStatusDTO);
    }

}
