package com.hy.biz.dataResolver.parser.strategy;

import com.hy.biz.dataResolver.dto.BaseMessage;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser
 * @description
 * @create 2023-05-19 09:59
 **/
public interface MessageParserStrategy {
    BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage);
}

