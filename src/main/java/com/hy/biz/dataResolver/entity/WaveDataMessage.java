package com.hy.biz.dataResolver.entity;

import com.hy.biz.dataResolver.util.WaveDataParserHelper;
import com.hy.domain.WaveData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @package com.hy.idds.biz.dataResolver.MessageEntity
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
    private int dataPacketLength;

    /**
     * 波形数据
     */
    private String waveData;

    /**
     * 波形起始时间
     */
    private String waveStartTime;

    /**
     * 波形数据总长度
     */
    private int waveDataLength;

    /**
     * 当前数据报段号
     */
    private int segmentNumber;

    /**
     * 数据总包数
     */
    private int dataPacketNumber;

    /**
     * 备用
     */
    private String reserved;

    public WaveData transform(WaveData waveData, WaveDataParserHelper waveDataParserHelper, long timeStamp, String deviceCode) {

        int segmentNumber = this.getSegmentNumber();
        int dataPacketNumber = this.getDataPacketNumber();

        if (segmentNumber == 1) {
            waveData = new WaveData();
            waveDataParserHelper.setWaveDataProperties(waveData, this, timeStamp, deviceCode);
        } else if (segmentNumber <= dataPacketNumber) {
            waveDataParserHelper.appendWaveData(waveData, this);
        }

        return waveData;
    }


}

