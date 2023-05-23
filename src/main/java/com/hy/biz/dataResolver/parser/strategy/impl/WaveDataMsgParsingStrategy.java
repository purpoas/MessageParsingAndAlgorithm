package com.hy.biz.dataResolver.parser.strategy.impl;

import com.hy.biz.dataResolver.dto.BaseMessage;
import com.hy.biz.dataResolver.dto.WaveDataMessage;
import com.hy.biz.dataResolver.parser.strategy.MessageParserStrategy;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hy.biz.dataResolver.constants.MessageConstants.TIME_LENGTH;
import static com.hy.biz.dataResolver.util.DateTimeUtil.parseDateTimeToStr;
import static com.hy.biz.dataResolver.util.TypeConverter.byteArrToStr;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.parser.strategy.impl
 * @description
 * @create 2023-05-22 17:43
 **/
@Slf4j
public class WaveDataMsgParsingStrategy implements MessageParserStrategy {

    private final StringBuilder waveDataStr = new StringBuilder();

    @Override
    public BaseMessage parse(ByteBuffer contentBuffer, BaseMessage specificMessage) {

        WaveDataMessage message = new WaveDataMessage();

        message.setFrameType(specificMessage.getFrameType());
        message.setMessageType(specificMessage.getMessageType());

        message.setDataPacketLength(contentBuffer.getShort());
        log.info("这段报文的波形数据长度： {}", message.getDataPacketLength());

        byte[] wave = new byte[message.getDataPacketLength()];
        contentBuffer.get(wave);

        byte[] waveStartTime = new byte[TIME_LENGTH];
        contentBuffer.get(waveStartTime);
        message.setWaveStartTime(parseDateTimeToStr(waveStartTime));

        message.setWaveDataLength(contentBuffer.getShort());
        log.info("波形数据总长度： {}", message.getWaveDataLength());

        message.setSegmentNumber(contentBuffer.get());
        log.info("当前报文段号： {}", message.getSegmentNumber());

        message.setDataPacketNumber(contentBuffer.get());
        log.info("总报文段数： {}", message.getDataPacketNumber());

        byte[] reserved = new byte[contentBuffer.remaining()];
        contentBuffer.get(reserved);
        message.setReserved(byteArrToStr(reserved));

        waveDataStr.append(IntStream.range(0, wave.length)
                .mapToObj(i -> Byte.toString(wave[i]))
                .collect(Collectors.joining(","))).append(",");

        if (message.getSegmentNumber() == message.getDataPacketNumber()) {
            if (waveDataStr.length() > 0) {
                waveDataStr.setLength(waveDataStr.length() - 1);
            }
            message.setWaveData(String.valueOf(waveDataStr));
            waveDataStr.setLength(0);
        }

        return message;
    }

}
