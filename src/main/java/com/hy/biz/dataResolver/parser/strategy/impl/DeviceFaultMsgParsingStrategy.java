package com.hy.biz.dataResolver.parser.strategy.impl;

import com.hy.biz.dataResolver.dto.BaseMessage;
import com.hy.biz.dataResolver.dto.DeviceFaultMessage;
import com.hy.biz.dataResolver.parser.strategy.MessageParserStrategy;

import java.nio.ByteBuffer;

import static com.hy.biz.dataResolver.constants.MessageConstants.TIME_LENGTH;
import static com.hy.biz.dataResolver.util.DateTimeUtil.parseDateTimeToInst;
import static com.hy.biz.dataResolver.util.TypeConverter.byteArrToStr;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy.impl
 * @description
 * @create 2023-05-22 17:39
 **/
public class DeviceFaultMsgParsingStrategy implements MessageParserStrategy {

    @Override
    public BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage) {
        DeviceFaultMessage message = new DeviceFaultMessage();
        byte[] time = new byte[TIME_LENGTH];
        buffer.get(time);
        message.setFaultDataCollectionTime(parseDateTimeToInst(time));
        int remaining = buffer.remaining();
        byte[] info = new byte[remaining];
        buffer.get(info);
        message.setDeviceFaultInfo(byteArrToStr(info));

        return message;
    }

}
