package com.hy.biz.dataResolver.parser.strategy;

import com.google.gson.JsonObject;
import com.hy.biz.dataResolver.parser.ParserHelper;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy
 * @description
 * @create 2023-05-23 09:27
 **/
public interface CtrlMsgParserStrategy {
    JsonObject parse(ByteBuffer buffer, ParserHelper parserHelper, String messageSignature, String deviceCode, long timeStamp);
}
