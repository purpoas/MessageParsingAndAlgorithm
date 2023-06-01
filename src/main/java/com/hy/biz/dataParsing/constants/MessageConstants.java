package com.hy.biz.dataParsing.constants;

/**
 *
 * 报文解析相关常量
 *
 * @package com.hy.idds.biz.dataParsing.constants
 * @author shiwentao
 * @create 2023-04-11 17:30
 **/
public final class MessageConstants {

    /**
     * 报文数据
     */
    public static final short HEADER = 0x5566;  //报文头
    public static final int HEADER_LENGTH = 2;  //Header长度
    public static final int ID_LENGTH = 17;  //ID号长度
    public static final int TIME_LENGTH = 12;  //时间相关数据长度
    public static final int MONITORING_TERMINAL_NAME_LENGTH = 50;  //监测终端名称长度
    public static final int MONITORING_TERMINAL_MODEL_LENGTH = 10;  //监测终端型号长度
    public static final int MANUFACTURER_LENGTH = 50;  //设备生产厂家长度
    public static final int FACTORY_SERIAL_NUMBER_LENGTH = 20;  //设备出厂编号长度
    public static final int RESERVED_LENGTH = 30;  //报文备用长度
    public static final int SIMPLE_DATE_LENGTH = 4;  //日期相关数据长度
    public static final int CHECK_SUM_LENGTH = 2;  //校验和长度


    /**
     * 控制报文
     */
    public static final byte MESSAGE_STATUS_SUCCESSFUL = 0x00;  //控制报文操作成功编号
    public static final byte MESSAGE_STATUS_FAILED = (byte) 0xFF;  //控制报文操作失败编号
    public static final String OPERATION_SUCCESSFUL = "操作成功";
    public static final String OPERATION_FAILED = "操作失败";
    public static final String UNKNOWN_OPERATION_RESULT = "操作结果未知";



    /**
     * 异常
     */
    public static final String ILLEGAL_MESSAGE_SIGNATURE_ERROR = "非法报文签名";
    public static final String ILLEGAL_HEADER_ERROR = "非法报文头";
    public static final String UNPARSED_DATA_ERROR = "部分数据未被解析";
    public static final String FAILED_TO_INSTANTIATE_MSG_CLASS = "不支持该数据类型";
    public static final String ILLEGAL_SUBSCRIBED_MESSAGE_SIGNATURE_ERROR = "无法识别订阅频道收到的消息类型（目前只支持解析设备上线通知，及设备的控制数据报文）";
    public static final String JSON_PROCESSING_ERROR = "JSON 数据解析失败";
    public static final String JSON_TO_DTO_ERROR = "接收到的 JSON 字符串无法转换为MessageDTO对象";
    public static final String MESSAGE_TO_ENTITY_ERROR = "报文实体类无法转换成入库实体类";
    public static final String SUB_MESSAGE_PARSING_ERROR = "订阅频道中的数据解析失败";
    public static final String EMPTY_MESSAGE_ERROR = "空报文";

    private MessageConstants() {}
}


