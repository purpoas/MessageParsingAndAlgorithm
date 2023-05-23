package com.hy.biz.dataResolver.dto;

import com.hy.biz.dataResolver.parser.ParserHelper;
import com.hy.domain.WaveData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shiwentao
 * @package com.hy.idds.biz.dataResolver.MessageEntity
 * @description
 * @create 2023-04-21 10:13
 **/
@Slf4j
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

    public WaveData transform(ParserHelper parserHelper, long timeStamp, String deviceCode) {
        return parserHelper.setWaveDataProperty(this, timeStamp, deviceCode);
    }


}
