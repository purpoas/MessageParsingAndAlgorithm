package com.hy.biz.dataResolver.parser.strategy.impl;

import com.google.gson.JsonObject;
import com.hy.biz.dataResolver.parser.ParserHelper;
import com.hy.biz.dataResolver.parser.strategy.CtrlMsgParserStrategy;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy.impl
 * @description
 * @create 2023-05-23 09:49
 **/
@Component
public class DeviceHistoricalDataRspMsgStrategy implements CtrlMsgParserStrategy {

    @Override
    public JsonObject parse(ByteBuffer buffer, ParserHelper parserHelper, String messageSignature, String deviceCode, long timeStamp) {
        JsonObject deviceHistoricalDataRspMsg = parserHelper.createJsonMsg(buffer, messageSignature, deviceCode, timeStamp, "设备历史数据查询");

        JsonObject params = new JsonObject();
        params.addProperty("historicalDataNum", buffer.getShort());
        params.addProperty("historicalTravellingWaveCurrentDataNum", buffer.getShort());
        params.addProperty("historicalPowerFrequencyCurrentDataNum", buffer.getShort());
        params.addProperty("historicalElectricalFieldVoltageDataNum", buffer.getShort());

        deviceHistoricalDataRspMsg.add("param", params);

        return deviceHistoricalDataRspMsg;
    }


}
