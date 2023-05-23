package com.hy.biz.dataResolver.parser.strategy.impl;

import com.hy.biz.dataResolver.dto.BaseMessage;
import com.hy.biz.dataResolver.dto.DeviceInfoMessage;
import com.hy.biz.dataResolver.parser.strategy.MessageParserStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

import static com.hy.biz.dataResolver.constants.MessageConstants.*;
import static com.hy.biz.dataResolver.constants.MessageConstants.RESERVED_LENGTH;
import static com.hy.biz.dataResolver.util.DateTimeUtil.parseDateToStr;
import static com.hy.biz.dataResolver.util.TypeConverter.byteArrToStr;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy.impl
 * @description
 * @create 2023-05-22 17:51
 **/
public class DeviceInfoMsgParsingStrategy implements MessageParserStrategy {

    @Override
    public BaseMessage parse(ByteBuffer buffer, BaseMessage specificMessage) {

        DeviceInfoMessage message = new DeviceInfoMessage();

        byte[] terminalName = new byte[MONITORING_TERMINAL_NAME_LENGTH];
        buffer.get(terminalName);
        message.setMonitoringTerminalName(byteArrToStr(terminalName));
        byte[] model = new byte[MONITORING_TERMINAL_MODEL_LENGTH];
        buffer.get(model);
        message.setMonitoringTerminalModel(byteArrToStr(model));
        message.setMonitoringTerminalInfoVersion(new BigDecimal(String.valueOf(buffer.getFloat()))
                .setScale(4, RoundingMode.HALF_UP)
                .toString());
        byte[] manufacturer = new byte[MANUFACTURER_LENGTH];
        buffer.get(manufacturer);
        message.setManufacturer(byteArrToStr(manufacturer));
        byte[] date = new byte[SIMPLE_DATE_LENGTH];
        buffer.get(date);
        message.setProductionDate(parseDateToStr(date));
        byte[] serialNumber = new byte[FACTORY_SERIAL_NUMBER_LENGTH];
        buffer.get(serialNumber);
        message.setFactoryNumber(Long.toString(ByteBuffer.wrap(serialNumber).order(LITTLE_ENDIAN).getLong()));
        byte[] reserved = new byte[RESERVED_LENGTH];
        buffer.get(reserved);
        message.setReserved(byteArrToStr(reserved));

        return message;
    }

}
