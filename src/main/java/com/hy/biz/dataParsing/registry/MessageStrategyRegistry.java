package com.hy.biz.dataParsing.registry;

import com.hy.biz.dataParsing.constants.FrameType;
import com.hy.biz.dataParsing.constants.MessageType;
import com.hy.biz.dataParsing.parser.strategy.MessageParserStrategy;
import com.hy.biz.dataParsing.parser.strategy.impl.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 报文解析器的注册类
 *
 * @author shiwentao
 * @package com.hy.biz.dataParsing.registry
 * @create 2023-05-19 12:00
 **/
public class MessageStrategyRegistry {

    private static final Map<String, MessageParserStrategy> MESSAGE_STRATEGY_MAP = new HashMap<>();
    private static final Map<String, MessageParserStrategy> UNMODIFIABLE_MESSAGE_STRATEGY_MAP;

    static {
        registerMessageClass();
        UNMODIFIABLE_MESSAGE_STRATEGY_MAP = Collections.unmodifiableMap(MESSAGE_STRATEGY_MAP);
    }

    /**
     * Registers all message classes in the MESSAGE_MAP.
     */
    private static void registerMessageClass() {
        MESSAGE_STRATEGY_MAP.put(FrameType.MONITORING_DATA_REPORT + ":" + MessageType.TRAVELLING_WAVE_CURRENT, new WaveDataMsgParsingStrategy());
        MESSAGE_STRATEGY_MAP.put(FrameType.MONITORING_DATA_REPORT + ":" + MessageType.FAULT_CURRENT, new WaveDataMsgParsingStrategy());
        MESSAGE_STRATEGY_MAP.put(FrameType.MONITORING_DATA_REPORT + ":" + MessageType.FAULT_VOLTAGE, new WaveDataMsgParsingStrategy());
        MESSAGE_STRATEGY_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.HEARTBEAT, new HeartBeatMsgParsingStrategy());
        MESSAGE_STRATEGY_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.BASIC_INFO, new DeviceInfoMsgParsingStrategy());
        MESSAGE_STRATEGY_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.WORKING_CONDITION, new WorkStatusMsgParsingStrategy());
        MESSAGE_STRATEGY_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.DEVICE_FAULT, new DeviceFaultMsgParsingStrategy());
        MESSAGE_STRATEGY_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.DEVICE_STATUS, new DeviceStatusMsgParsingStrategy());
    }

    public static Map<String, MessageParserStrategy> getMessageStrategyMap() {
        return UNMODIFIABLE_MESSAGE_STRATEGY_MAP;
    }

    private MessageStrategyRegistry() {}
}
