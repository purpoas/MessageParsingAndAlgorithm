package com.hy.biz.dataPush.subscriber.handler;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing
 * @description
 * @create 2023-05-10 14:37
 **/
public interface MessageHandler {
    boolean canHandle(JsonNode message);
    void handle(JsonNode message, String channel);
}

