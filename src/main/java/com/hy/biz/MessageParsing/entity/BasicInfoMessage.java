package com.hy.biz.MessageParsing.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BasicInfoMessage extends BaseMessage {

    /**
     * 监测终端名称
     */
    private byte[] monitoringTerminalName;

    /**
     * 监测终端型号
     */
    private byte[] monitoringTerminalModel;

    /**
     * 监测终端基本信息版本号
     */
    private float monitoringTerminalInfoVersion;

    /**
     * 生产厂家
     */
    private byte[] manufacturer;

    /**
     * 生产日期
     */
    private float productionDate;

    /**
     * 出厂编号
     */
    private byte[] factoryNumber;

    /**
     * 备用
     */
    private byte[] reserved;

}

