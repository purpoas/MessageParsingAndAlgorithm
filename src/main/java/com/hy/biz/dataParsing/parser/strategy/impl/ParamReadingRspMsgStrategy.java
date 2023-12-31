package com.hy.biz.dataParsing.parser.strategy.impl;

import com.google.gson.JsonObject;
import com.hy.biz.dataParsing.parser.ParserHelper;
import com.hy.biz.dataParsing.parser.strategy.CtrlMsgParserStrategy;
import com.hy.biz.dataParsing.registry.ParamCodeRegistry;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing.parser.strategy.impl
 * @create 2023-05-23 09:46
 **/
public class ParamReadingRspMsgStrategy implements CtrlMsgParserStrategy {

    private final Map<String, String> PARAM_CODE_MAP = ParamCodeRegistry.getParamCodeMap();

    @Override
    public JsonObject parse(ByteBuffer buffer, ParserHelper parserHelper, String messageSignature, String deviceCode, long timeStamp) {
        JsonObject paramReadingRspMsg = parserHelper.createJsonMsg(buffer, messageSignature, deviceCode, timeStamp, "参数读取");

        JsonObject params = new JsonObject();
        for (int i = 0, paramNum = buffer.getShort(); i < paramNum; i++) {
            String paramCodeHex = String.format("0x%04x", buffer.getShort() & 0xffff);
            paramCodeHex = paramCodeHex.substring(0, 2) + paramCodeHex.substring(2).toUpperCase();
            String paramName = PARAM_CODE_MAP.get(paramCodeHex);

            byte[] rawParam = new byte[4];
            buffer.get(rawParam);

            if (paramName.equals("waveCallbackTime") || paramName.equals("powerCallbackTime") || paramName.equals("groundCallbackTime") || paramName.equals("workReportTime")) {
                String paramVal = String.format("%02d:%02d:%02d", rawParam[1], rawParam[2], rawParam[3]);
                params.addProperty(paramName, paramVal);
            } else {
//                int result = 0;
//                for (int j = 0; j < 4; j++) {
//                    result |= (rawParam[j] & 0xFF) << (j * 8);  // 高位解析
//                }
                int result = 0;
                for (int j = rawParam.length - 1; j >= 0; j--) {
                    result |= (rawParam[j] & 0xFF) << ((rawParam.length - 1 - j) * 8);  // 低位解析
                }
                params.addProperty(paramName, result);
            }

        }
        paramReadingRspMsg.add("param", params);

        return paramReadingRspMsg;
    }


}
