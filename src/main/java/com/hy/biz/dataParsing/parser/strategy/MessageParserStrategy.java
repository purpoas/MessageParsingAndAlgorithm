package com.hy.biz.dataParsing.parser.strategy;

import com.hy.biz.dataParsing.dto.BaseMessage;

import java.nio.ByteBuffer;

/**
 * ============================
 * 阻塞队列报文解析 Strategy     ｜
 * ============================
 *
 * @author shiwentao
 * @package com.hy.biz.dataParsing.parser
 * @create 2023-05-19 09:59
 **/
public interface MessageParserStrategy {

    /**
     * @param buffer          buffer
     * @param specificMessage 具体报文实体类
     * @param timeStamp       时间戳
     * @return 具体报文实体类
     */
    BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage, long timeStamp);


}

