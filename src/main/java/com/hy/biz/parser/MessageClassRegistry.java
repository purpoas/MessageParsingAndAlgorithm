package com.hy.biz.parser;

import com.hy.biz.parser.entity.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.hy.biz.parser.constants.FrameType.MONITORING_DATA_REPORT;
import static com.hy.biz.parser.constants.FrameType.WORK_STATUS_REPORT;
import static com.hy.biz.parser.constants.MessageType.*;

/**
 * @description A utility class that manages the registration of message classes.
 *              Provides a static, unmodifiable map containing the message classes.
 */
public class MessageClassRegistry {

    private static final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = new HashMap<>();
    private static final Map<String, Class<? extends BaseMessage>> UNMODIFIABLE_MESSAGE_MAP;

    static {
        registerMessageClass();
        UNMODIFIABLE_MESSAGE_MAP = Collections.unmodifiableMap(MESSAGE_MAP);
    }

    /**
     * Registers all message classes in the MESSAGE_MAP.
     */
    private static void registerMessageClass() {
        MESSAGE_MAP.put(MONITORING_DATA_REPORT + ":" + TRAVELLING_WAVE_CURRENT, TravellingWaveCurrentMessage.class);
        MESSAGE_MAP.put(MONITORING_DATA_REPORT + ":" + FAULT_CURRENT, FaultCurrentMessage.class);
        MESSAGE_MAP.put(MONITORING_DATA_REPORT + ":" + FAULT_VOLTAGE, FaultVoltageMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + HEARTBEAT, HeartBeatMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + BASIC_INFO, BasicInfoMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + WORKING_CONDITION, WorkingConditionMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + DEVICE_FAULT, DeviceFaultMessage.class);
        MESSAGE_MAP.put(WORK_STATUS_REPORT + ":" + DEVICE_STATUS, DeviceStatusMessage.class);
    }

    public static Map<String, Class<? extends BaseMessage>> getMessageMap() {
        return UNMODIFIABLE_MESSAGE_MAP;
    }

    private MessageClassRegistry() {}

}

