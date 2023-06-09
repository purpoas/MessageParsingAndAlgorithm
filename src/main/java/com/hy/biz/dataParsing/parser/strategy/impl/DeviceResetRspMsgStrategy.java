package com.hy.biz.dataParsing.parser.strategy.impl;

import com.google.gson.JsonObject;
import com.hy.biz.dataParsing.parser.ParserHelper;
import com.hy.biz.dataParsing.parser.strategy.CtrlMsgParserStrategy;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing.parser.strategy.impl
 * @description
 * @create 2023-05-23 09:42
 **/
public class DeviceResetRspMsgStrategy implements CtrlMsgParserStrategy {

    @Override
    public JsonObject parse(ByteBuffer buffer, ParserHelper parserHelper, String messageSignature, String deviceCode, long timeStamp) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("status", true);
        jsonObject.addProperty("msg", "装置复位成功");
        jsonObject.addProperty("msgType", messageSignature);
        jsonObject.addProperty("timestamp", timeStamp / 1000);
        jsonObject.addProperty("deviceCode", deviceCode);

        return jsonObject;
    }


}
