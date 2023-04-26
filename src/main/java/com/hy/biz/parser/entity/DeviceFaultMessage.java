package com.hy.biz.parser.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceFaultMessage extends BaseMessage {

    /**
     * 故障数据采集时间
     */
    private byte[] faultDataCollectionTime;

    /**
     * 装置故障信息
     */
    private byte[] deviceFaultInfo;

}

