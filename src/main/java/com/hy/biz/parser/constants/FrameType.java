package com.hy.biz.parser.constants;

/**
 * @package com.hy.biz.parser.constants
 * @description 报文帧类型常量
 * @author shiwentao
 * @create 2023-04-21 17:52
 **/
public class FrameType {

    /**
     * 监测数据
     */
    public static final byte MONITORING_DATA_REPORT = 0x01;
    public static final byte MONITORING_DATA_ACK_REPORT = 0x02;

    /**
     * 控制数据
     */
    public static final byte CONTROL_REPORT = 0x03;
    public static final byte CONTROL_ACK_REPORT = 0x04;

    /**
     * 工作状态
     */
    public static final byte WORK_STATUS_REPORT = 0x05;
    public static final byte WORK_STATUS_ACK_REPORT = 0x06;

    public FrameType() {}

}
