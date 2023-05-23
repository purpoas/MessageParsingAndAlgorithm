package com.hy.biz.dataResolver.constants;

/**
 *
 * 报文类型
 *
 * @package com.hy.idds.biz.dataResolver.constants
 * @author shiwentao
 * @create 2023-04-21 17:54
 **/
public class MessageType {

    /**
     * 监测数据
     */
    public static final byte TRAVELLING_WAVE_CURRENT = 0x01;
    public static final byte FAULT_CURRENT = 0x03;
    public static final byte FAULT_VOLTAGE = 0x05;

    /**
     * 控制数据
     */
    public static final byte DEVICE_RESET = 0x01;
    public static final byte PARAMETER_SETTING = 0x03;
    public static final byte PARAMETER_READING = 0x05;
    public static final byte PROGRAM_UPGRADE = 0x07;
    public static final byte COLLECT_TRAVELING_WAVE_CURRENT_DATA = 0x13;
    public static final byte COLLECT_MAINS_FREQUENCY_CURRENT_DATA = 0x14;
    public static final byte QUERY_DEVICE_HISTORICAL_DATA = 0x15;
    public static final byte COLLECT_POWER_PLANT_VOLTAGE_DATA = 0x19;

    /**
     * 工作状态
     */
    public static final byte HEARTBEAT = 0x01;
    public static final byte BASIC_INFO = 0x03;
    public static final byte WORKING_CONDITION = 0x05;
    public static final byte DEVICE_FAULT = 0x07;
    public static final byte DEVICE_STATUS = 0x0A;

    private MessageType() {}

}
