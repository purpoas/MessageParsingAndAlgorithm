package com.hy.biz.parser.constants;

/**
 * @package com.hy.idds.biz.parser.constants
 * @description 报文解析相关常量
 * @author shiwentao
 * @create 2023-04-11 17:30
 **/
public final class MessageConstants {

    /**
     * 报文数据
     */
    public static final short HEADER = 0x5566;  //报文头
    public static final int ID_LENGTH = 17;  //ID号长度
    public static final int TIME_LENGTH = 12;  //时间相关数据长度
    public static final int MONITORING_TERMINAL_NAME_LENGTH = 50;  //监测终端名称长度
    public static final int MONITORING_TERMINAL_MODEL_LENGTH = 10;  //监测终端型号长度
    public static final int MANUFACTURER_LENGTH = 50;  //生产厂家长度
    public static final int FACTORY_SERIAL_NUMBER_LENGTH = 20;  //出厂编号长度
    public static final int RESERVED_LENGTH = 30;  //备用长度
    public static final int SIMPLE_DATE_LENGTH = 4;
    public static final byte MESSAGE_STATUS_SUCCESS = 0x00;
    public static final byte MESSAGE_STATUS_FAILURE = (byte) 0xFF;


    /**
     * 异常
     */
    public static final String ILLEGAL_MESSAGE_SIGNATURE_ERROR = "非法报文签名";
    public static final String ILLEGAL_HEADER_ERROR = "非法报文头";
    public static final String UNPARSED_DATA_ERROR = "部分数据未被解析";
    public static final String UNSUPPORTED_DATA_TYPE_ERROR = "不支持该数据类型";

    private MessageConstants() {}
}


