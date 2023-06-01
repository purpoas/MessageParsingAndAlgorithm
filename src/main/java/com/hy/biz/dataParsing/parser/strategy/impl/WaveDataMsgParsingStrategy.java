package com.hy.biz.dataParsing.parser.strategy.impl;

import com.hy.biz.dataParsing.dto.BaseMessage;
import com.hy.biz.dataParsing.dto.WaveDataMessage;
import com.hy.biz.dataParsing.parser.strategy.MessageParserStrategy;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hy.biz.dataParsing.constants.MessageConstants.TIME_LENGTH;
import static com.hy.biz.dataParsing.util.DateTimeUtil.parseDateTimeToStr;
import static com.hy.biz.dataParsing.util.TypeConverter.byteArrToStr;

/**
 * This strategy is responsible for parsing WaveDataMessage instances.
 * It builds wave data segments and when all the segments for a wave data message are ready, it concatenates them into a single string.
 */
@Slf4j
public class WaveDataMsgParsingStrategy implements MessageParserStrategy {

    private final TreeMap<Integer, String> waveSegments = new TreeMap<>();

    @Override
    public BaseMessage parse(ByteBuffer contentBuffer, BaseMessage specificMessage) {
        WaveDataMessage message = new WaveDataMessage();

        message.setFrameType(specificMessage.getFrameType());
        message.setMessageType(specificMessage.getMessageType());

        message.setDataPacketLength(contentBuffer.getShort());
        log.info("这段报文的波形数据长度： {}", message.getDataPacketLength());

        byte[] segmentedWaveData = new byte[message.getDataPacketLength()];
        contentBuffer.get(segmentedWaveData);

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

        setWaveData(segmentedWaveData, message);

        return message;
    }

    //====================private====================private====================private====================private=================

    private void setWaveData(byte[] segmentedWaveData, WaveDataMessage message) {
        String segmentedWaveDataStr = IntStream.range(0, segmentedWaveData.length)
                .mapToObj(i -> Byte.toString(segmentedWaveData[i])) // TODO 解析wavedata逻辑
                .collect(Collectors.joining(","));

        waveSegments.put(message.getSegmentNumber(), segmentedWaveDataStr);

        if (waveSegments.size() == message.getDataPacketNumber()) {
            StringBuilder waveDataStr = new StringBuilder();
            waveSegments.values().forEach(waveDataStr::append);
            message.setWaveData(waveDataStr.toString());
            waveSegments.clear();
        }
    }


}
