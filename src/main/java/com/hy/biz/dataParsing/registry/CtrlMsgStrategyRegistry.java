package com.hy.biz.dataParsing.registry;

import com.hy.biz.dataParsing.parser.strategy.CtrlMsgParserStrategy;
import com.hy.biz.dataParsing.parser.strategy.impl.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.hy.biz.dataParsing.constants.FrameType.CONTROL_ACK_REPORT;
import static com.hy.biz.dataParsing.constants.MessageType.*;

/**
 *
 * 控制报文解析器的注册类
 *
 * @author shiwentao
 * @package com.hy.biz.dataParsing.registry
 * @create 2023-05-23 10:08
 **/
public class CtrlMsgStrategyRegistry {

    private static final Map<String, CtrlMsgParserStrategy> CTRL_MESSAGE_STRATEGY_MAP = new HashMap<>();
    private static final Map<String, CtrlMsgParserStrategy> UNMODIFIABLE_CTRL_MESSAGE_STRATEGY_MAP;

    static {
        registerMessageClass();
        UNMODIFIABLE_CTRL_MESSAGE_STRATEGY_MAP = Collections.unmodifiableMap(CTRL_MESSAGE_STRATEGY_MAP);
    }

    private static void registerMessageClass() {
        CTRL_MESSAGE_STRATEGY_MAP.put(String.format("0x%02X:0x%02X",CONTROL_ACK_REPORT ,DEVICE_RESET), new DeviceResetRspMsgStrategy());
        CTRL_MESSAGE_STRATEGY_MAP.put(String.format("0x%02X:0x%02X",CONTROL_ACK_REPORT ,PARAMETER_SETTING), new ParamSettingRspMsgStrategy());
        CTRL_MESSAGE_STRATEGY_MAP.put(String.format("0x%02X:0x%02X",CONTROL_ACK_REPORT ,PARAMETER_READING), new ParamReadingRspMsgStrategy());
        CTRL_MESSAGE_STRATEGY_MAP.put(String.format("0x%02X:0x%02X",CONTROL_ACK_REPORT ,PROGRAM_UPGRADE), new ProgramUpgradeRspMsgStrategy());
        CTRL_MESSAGE_STRATEGY_MAP.put(String.format("0x%02X:0x%02X",CONTROL_ACK_REPORT ,QUERY_DEVICE_HISTORICAL_DATA), new DeviceHistoricalDataRspMsgStrategy());
    }

    public static Map<String, CtrlMsgParserStrategy> getCtrlMessageStrategyMap() {
        return UNMODIFIABLE_CTRL_MESSAGE_STRATEGY_MAP;
    }

    private CtrlMsgStrategyRegistry() {}


}
