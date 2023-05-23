package com.hy.biz.dataResolver.parser.strategy.impl;

import com.hy.biz.dataResolver.dto.BaseMessage;
import com.hy.biz.dataResolver.dto.WorkStatusMessage;
import com.hy.biz.dataResolver.parser.strategy.MessageParserStrategy;

import java.nio.ByteBuffer;

import static com.hy.biz.dataResolver.constants.MessageConstants.TIME_LENGTH;
import static com.hy.biz.dataResolver.util.DateTimeUtil.parseDateTimeToInst;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy.impl
 * @description
 * @create 2023-05-22 17:46
 **/
public class WorkStatusMsgParsingStrategy implements MessageParserStrategy {

    @Override
    public BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage) {

        WorkStatusMessage message = new WorkStatusMessage();

        byte[] UploadTime = new byte[TIME_LENGTH];
        buffer.get(UploadTime);
        message.setUploadTime(parseDateTimeToInst(UploadTime));
        buffer.position(buffer.position() + 3);
        message.setDeviceTemperature(buffer.getShort());
        message.setCurrentEffectiveValue(buffer.getShort());

        return message;
    }

}
