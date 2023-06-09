package com.hy.biz.dataParsing.parser.strategy.impl;

import com.hy.biz.dataParsing.dto.BaseMessage;
import com.hy.biz.dataParsing.dto.DeviceFaultMessage;
import com.hy.biz.dataParsing.parser.strategy.MessageParserStrategy;

import java.nio.ByteBuffer;
import java.time.Instant;

import static com.hy.biz.dataParsing.constants.MessageConstants.TIME_LENGTH;
import static com.hy.biz.dataParsing.util.TypeConverter.byteArrToStr;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing.parser.strategy.impl
 * @description
 * @create 2023-05-22 17:39
 **/
public class DeviceFaultMsgParsingStrategy implements MessageParserStrategy {

    @Override
    public BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage, long timeStamp) {
        DeviceFaultMessage message = new DeviceFaultMessage();
        byte[] time = new byte[TIME_LENGTH];
        buffer.get(time);
        message.setFaultDataCollectionTime(Instant.ofEpochMilli(timeStamp));
        int remaining = buffer.remaining();
        byte[] info = new byte[remaining];
        buffer.get(info);
        message.setDeviceFaultInfo(byteArrToStr(info));

        return message;
    }

}
