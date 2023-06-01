package com.hy.biz.dataParsing.parser.strategy;

import com.hy.biz.dataParsing.dto.BaseMessage;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing.parser
 * @create 2023-05-19 09:59
 **/
public interface MessageParserStrategy {

    /**
     * @param buffer buffer
     * @param specificMessage 具体报文实体类
     * @return 具体报文实体类
     */
    BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage);


}

