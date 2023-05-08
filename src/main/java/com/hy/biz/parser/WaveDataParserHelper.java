package com.hy.biz.parser;

import com.hy.biz.parser.entity.WaveDataMessage;
import com.hy.config.HyConfigProperty;
import com.hy.domain.WaveData;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.hy.biz.parser.util.TypeConverter.byteArrToStr;
import static com.hy.biz.parser.util.DateTimeUtil.*;

/**
 * @author shiwentao
 * @package com.hy.biz.parser.util
 * @description
 * @create 2023-05-06 10:57
 **/
@Component
public class WaveDataParserHelper {
    private final HyConfigProperty hyConfigProperty;

    public WaveDataParserHelper(HyConfigProperty hyConfigProperty) {
        this.hyConfigProperty = hyConfigProperty;
    }

    protected void setWaveDataProperties(WaveData waveData, WaveDataMessage message, long timeStamp, String deviceCode) {
        waveData.setCollectionTime(parseDateTimeToInst(timeStamp));
        byte frameType = message.getFrameType();
        byte messageType = message.getMessageType();
        waveData.setType((int) messageType);

        String waveDataCode = generateWaveDataCode(timeStamp, frameType, messageType, deviceCode);
        waveData.setCode(waveDataCode);

        waveData.setLength((long) message.getWaveDataLength());
        waveData.setHeadTime(parseDateTimeToStr(message.getWaveStartTime()));
        waveData.setSampleRate(hyConfigProperty.getConstant().getTravelSampleRate());
        waveData.setThreshold(hyConfigProperty.getConstant().getTravelThreshold());

        int relafalg = parseRelaFalg();
        waveData.setRelaFlag(relafalg);

        waveData.setData(Arrays.toString(message.getWaveData()));
        waveData.setRemark(byteArrToStr(message.getReserved()));
    }

    protected void appendWaveData(WaveData waveData, WaveDataMessage waveDataMessage) {
        waveData.setData(waveData.getData() + byteArrToStr(waveDataMessage.getWaveData()));
    }

    private String generateWaveDataCode(long timeStamp, byte frameType, byte messageType, String deviceCode) {
        byte[] dateTimeBytes = longToDateTimeBytes(timeStamp);
        String ymd = String.format("20%02d%02d%02d", dateTimeBytes[0] & 0xFF, dateTimeBytes[1] & 0xFF, dateTimeBytes[2] & 0xFF);
        String hms = String.format("%02d%02d%02d", dateTimeBytes[3] & 0xFF, dateTimeBytes[4] & 0xFF, dateTimeBytes[5] & 0xFF);
        String ns = String.format("%03d%03d%03d",
                ((dateTimeBytes[6] & 0xFF) << 8) + (dateTimeBytes[7] & 0xFF),
                ((dateTimeBytes[8] & 0xFF) << 8) + (dateTimeBytes[9] & 0xFF),
                ((dateTimeBytes[10] & 0xFF) << 8) + (dateTimeBytes[11] & 0xFF)
        );

        String frameTypeStr = String.format("%02d", frameType);
        String messageTypeStr = String.format("%02d", messageType);

        return String.format("W%s-%s-%s-%s%s-%s",
                ymd, hms, ns, frameTypeStr, messageTypeStr, deviceCode);
    }

    //todo 需结合算法
    private Integer parseRelaFalg() {
        return 1;
    }

}
