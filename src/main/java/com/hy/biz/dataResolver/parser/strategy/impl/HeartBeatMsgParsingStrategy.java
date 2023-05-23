package com.hy.biz.dataResolver.parser.strategy.impl;

import com.hy.biz.dataResolver.dto.BaseMessage;
import com.hy.biz.dataResolver.parser.strategy.MessageParserStrategy;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy.impl
 * @description
 * @create 2023-05-22 17:54
 **/
public class HeartBeatMsgParsingStrategy implements MessageParserStrategy {

    @Override
    public BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage) {
        return null;
    }


}
