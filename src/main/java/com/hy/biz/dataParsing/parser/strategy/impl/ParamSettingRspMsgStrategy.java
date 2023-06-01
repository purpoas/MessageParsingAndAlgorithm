package com.hy.biz.dataParsing.parser.strategy.impl;

import com.google.gson.JsonObject;
import com.hy.biz.dataParsing.parser.ParserHelper;
import com.hy.biz.dataParsing.parser.strategy.CtrlMsgParserStrategy;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing.parser.strategy.impl
 * @description
 * @create 2023-05-23 09:45
 **/
@Component
public class ParamSettingRspMsgStrategy implements CtrlMsgParserStrategy {

    @Override
    public JsonObject parse(ByteBuffer buffer, ParserHelper parserHelper, String messageSignature, String deviceCode, long timeStamp) {
        return parserHelper.createJsonMsg(buffer, messageSignature, deviceCode, timeStamp, "参数设置");
    }


}
