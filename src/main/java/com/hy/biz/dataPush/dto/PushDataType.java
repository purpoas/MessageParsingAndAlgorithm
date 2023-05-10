package com.hy.biz.dataPush.dto;

/**
 * 消息推送数据类型
 */
public enum PushDataType {

    HEARTBEAT,          //设备心跳
    ERRORLOG,           //设备故障
    WORK_STATE,         //设备工况
    DEVUCE_INFO,        //设备基本信息
    DEVICE_PARAM,       //设备响应参数
    WAVE,               //波形
    FAULT_WAVE,         //故障波形
    ANALYSIS_RESULT,    //分析结果
    // 以下是汇源扩展协议
    GPS,                //Gps
    DEVICE_STATUS,      //设备状态
    DEVICE_WORKHOURS,   //设备工作时长
    HISTORY_DATA,       //历史数据
    DEVICE_DAILY_HISTORY_DATA   //历史数据条数

}
