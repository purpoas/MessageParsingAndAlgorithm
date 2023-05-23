package com.hy.biz.dataResolver.registry;

import com.hy.biz.dataResolver.constants.FrameType;
import com.hy.biz.dataResolver.constants.MessageType;
import com.hy.biz.dataResolver.dto.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 报文类的注册类
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
        MESSAGE_MAP.put(FrameType.MONITORING_DATA_REPORT + ":" + MessageType.TRAVELLING_WAVE_CURRENT, TravellingWaveCurrentMessage.class);
        MESSAGE_MAP.put(FrameType.MONITORING_DATA_REPORT + ":" + MessageType.FAULT_CURRENT, FaultCurrentMessage.class);
        MESSAGE_MAP.put(FrameType.MONITORING_DATA_REPORT + ":" + MessageType.FAULT_VOLTAGE, FaultVoltageMessage.class);
        MESSAGE_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.HEARTBEAT, HeartBeatMessage.class);
        MESSAGE_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.BASIC_INFO, DeviceInfoMessage.class);
        MESSAGE_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.WORKING_CONDITION, WorkStatusMessage.class);
        MESSAGE_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.DEVICE_FAULT, DeviceFaultMessage.class);
        MESSAGE_MAP.put(FrameType.WORK_STATUS_REPORT + ":" + MessageType.DEVICE_STATUS, DeviceStatusMessage.class);
    }

    public static Map<String, Class<? extends BaseMessage>> getMessageMap() {
        return UNMODIFIABLE_MESSAGE_MAP;
    }

    private MessageClassRegistry() {}

}

