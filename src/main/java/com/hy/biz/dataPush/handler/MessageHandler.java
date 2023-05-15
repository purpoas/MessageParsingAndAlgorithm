package com.hy.biz.dataPush.handler;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver
 * @description
 * @create 2023-05-10 14:37
 **/
public interface MessageHandler {
    boolean canHandle(JsonNode message);
    void handle(JsonNode message, String channel);
}

