package com.hy.biz.dataResolver.parser.strategy;

import com.hy.biz.dataResolver.dto.BaseMessage;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser
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

