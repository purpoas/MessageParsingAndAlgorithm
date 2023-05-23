package com.hy.biz.dataResolver.parser.strategy;

import com.google.gson.JsonObject;
import com.hy.biz.dataResolver.parser.ParserHelper;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy
 * @create 2023-05-23 09:27
 **/
public interface CtrlMsgParserStrategy {

    /**
     * 控制报文解析策略抽象类
     * @param buffer buffer
     * @param parserHelper 解析Helper
     * @param messageSignature 帧类型+报文类型
     * @param deviceCode 设备编号
     * @param timeStamp 时间戳
     * @return jsonObject 用于发布到 Redis 订阅频道
     */
    JsonObject parse(ByteBuffer buffer, ParserHelper parserHelper, String messageSignature, String deviceCode, long timeStamp);


}
