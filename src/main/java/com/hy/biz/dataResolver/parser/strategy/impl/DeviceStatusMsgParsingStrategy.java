package com.hy.biz.dataResolver.parser.strategy.impl;

import com.hy.biz.dataResolver.constants.MessageConstants;
import com.hy.biz.dataResolver.dto.BaseMessage;
import com.hy.biz.dataResolver.dto.DeviceStatusMessage;
import com.hy.biz.dataResolver.parser.strategy.MessageParserStrategy;

import java.nio.ByteBuffer;

import static com.hy.biz.dataResolver.util.DateTimeUtil.parseDateTimeToInst;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy.impl
 * @description
 * @create 2023-05-22 17:52
 **/
public class DeviceStatusMsgParsingStrategy implements MessageParserStrategy {

    @Override
    public BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage) {

        DeviceStatusMessage message = new DeviceStatusMessage();

        message.setFrameType(specificMessage.getFrameType());
        message.setMessageType(specificMessage.getMessageType());
        byte[] time = new byte[MessageConstants.TIME_LENGTH];
        buffer.get(time);
        message.setDataCollectionUploadTime(parseDateTimeToInst(time));
        message.setSolarChargingCurrent(buffer.getShort());
        message.setPhasePowerCurrent(buffer.getShort());
        message.setDeviceWorkingVoltage(buffer.getShort());
        message.setDeviceWorkingCurrent(buffer.getShort());
        message.setBatteryVoltage(buffer.getShort());
        message.setReserved(buffer.get());
        message.setSolarPanelAVoltage(buffer.getShort());
        message.setSolarPanelBVoltage(buffer.getShort());
        message.setSolarPanelCVoltage(buffer.getShort());
        message.setPhasePowerVoltage(buffer.getShort());
        message.setChipTemperature(buffer.getShort());
        message.setMainBoardTemperature(buffer.getShort());
        message.setDeviceSignalStrength(buffer.getShort());
        message.setGpsLatitude(buffer.getFloat());
        message.setGpsLongitude(buffer.getFloat());

        return message;
    }

}
