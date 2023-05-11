package com.hy.biz.dataPush.dto;

/**
 * 消息推送数据类型
 */
public enum PushDataType {

    DEVICE_FAULT,              //设备故障
    DEVICE_INFO,               //设备基本信息
    DEVICE_STATUS,             //设备状态
    FAULT_CURRENT,             //故障行波电流
    FAULT_VOLTAGE,             //故障电压
    HEARTBEAT,                 //设备心跳
    TRAVELLING_WAVE_CURRENT,   //行波电流
    WAVE,                      //波形数据
    WORK_STATUS,               //线路工况

}
