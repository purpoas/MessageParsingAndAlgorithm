package com.hy.biz.MessageParsing.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @package com.hy.idds.biz.MessageParsing.util
 * @description
 * @author shiwentao
 * @create 2023-04-23 17:13
 **/
public class DateTimeUtil {
    public static String parseDateTimeToStr(byte[] rawData) {
        ZonedDateTime zonedDateTime = parseDateTimeToZonedDateTime(rawData);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
        return zonedDateTime.format(formatter);
    }

    public static Instant parseDateTimeToInst(byte[] rawData) {
        ZonedDateTime zonedDateTime = parseDateTimeToZonedDateTime(rawData);
        return zonedDateTime.toInstant();
    }

    private static ZonedDateTime parseDateTimeToZonedDateTime(byte[] rawData) {
        ByteBuffer buffer = ByteBuffer.wrap(rawData);
        buffer.order(ByteOrder.BIG_ENDIAN);

        int year = 2000 + buffer.get();
        int month = buffer.get();
        int day = buffer.get();
        int hour = buffer.get();
        int minute = buffer.get();
        int second = buffer.get();
        int millis = buffer.getShort();
        int micros = buffer.getShort();
        int nanos = buffer.getShort();

        int totalNanos = millis * 1_000_000 + micros * 1_000 + nanos;

        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second, totalNanos);
        return dateTime.atZone(ZoneId.systemDefault());
    }
}
