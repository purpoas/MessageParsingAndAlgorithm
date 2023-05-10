package com.hy.biz.parser.entity;

import com.hy.biz.parser.util.WaveDataParserHelper;
import com.hy.domain.WaveData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @package com.hy.idds.biz.parser.MessageEntity
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

    public WaveData transform(WaveData waveData, WaveDataParserHelper waveDataParserHelper, long timeStamp, String deviceCode) {
        byte segmentNumber = this.getSegmentNumber();
        byte dataPacketNumber = this.getDataPacketNumber();

        if (segmentNumber == 1) {
            waveData = new WaveData();
            waveDataParserHelper.setWaveDataProperties(waveData, this, timeStamp, deviceCode);
        } else if (segmentNumber <= dataPacketNumber) {
            waveDataParserHelper.appendWaveData(waveData, this);
        }

        return waveData;
    }

}

