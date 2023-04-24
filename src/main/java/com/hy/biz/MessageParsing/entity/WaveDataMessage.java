package com.hy.biz.MessageParsing.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @package com.hy.idds.biz.MessageParsing.MessageEntity
 * @description
 * @author shiwentao
 * @create 2023-04-21 10:13
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class WaveDataMessage extends BaseMessage {

    /**
     * 当前数据包段包含的波形数据长度
     */
    private short dataPacketLength;

    /**
     * 波形数据
     */
    private byte[] waveData;

    /**
     * 波形起始时间
     */
    private byte[] waveStartTime;

    /**
     * 波形数据总长度
     */
    private short waveDataLength;

    /**
     * 当前数据报段号
     */
    private byte segmentNumber;

    /**
     * 数据总包数
     */
    private byte dataPacketNumber;

    /**
     * 备用
     */
    private byte[] reserved;

}

