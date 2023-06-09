package com.hy.biz.dataParsing.parser.strategy.impl;

import com.hy.biz.dataParsing.dto.BaseMessage;
import com.hy.biz.dataParsing.dto.WorkStatusMessage;
import com.hy.biz.dataParsing.parser.strategy.MessageParserStrategy;

import java.nio.ByteBuffer;
import java.time.Instant;

import static com.hy.biz.dataParsing.constants.MessageConstants.TIME_LENGTH;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing.parser.strategy.impl
 * @description
 * @create 2023-05-22 17:46
 **/
public class WorkStatusMsgParsingStrategy implements MessageParserStrategy {

    @Override
    public BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage, long timeStamp) {
        WorkStatusMessage message = new WorkStatusMessage();

        byte[] UploadTime = new byte[TIME_LENGTH];
        buffer.get(UploadTime);
        message.setUploadTime(Instant.ofEpochMilli(timeStamp));
        buffer.position(buffer.position() + 3);
        message.setDeviceTemperature(buffer.getShort());
        message.setCurrentEffectiveValue(buffer.getShort());

        return message;
    }

}
