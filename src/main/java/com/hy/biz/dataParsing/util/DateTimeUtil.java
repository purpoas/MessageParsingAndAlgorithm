package com.hy.biz.dataParsing.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 *=======================
 * 日期时间转换工具类      ｜
 *=======================
 *
 * @author shiwentao
 * @package com.hy.idds.biz.dataParsing.algorithmUtil
 * @create 2023-04-23 17:13
 **/
public class DateTimeUtil {

    public static String parseDateBytesToStr(byte[] timeStamp) {
        String dateStr = new String(timeStamp, StandardCharsets.UTF_8);
        return dateStr.substring(0, 4);
    }

    public static String parseWaveDateBytesToStr(byte[] timestamp) {
        ZonedDateTime zonedDateTime = parseDateTimeToZonedDateTime(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
        return zonedDateTime.format(formatter);
    }

    public static byte[] parseLongDateToBytes(long timestamp) {
        LocalDateTime dateTime = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        int year = dateTime.getYear() % 100;
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = dateTime.getSecond();
        int nanos = dateTime.getNano();

        ByteBuffer buffer = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) year);
        buffer.put((byte) month);
        buffer.put((byte) day);
        buffer.put((byte) hour);
        buffer.put((byte) minute);
        buffer.put((byte) second);
        buffer.putShort((short) (nanos / 1_000_000));
        buffer.putShort((short) ((nanos / 1_000) % 1_000));
        buffer.putShort((short) (nanos % 1_000));

        return buffer.array();
    }


    //=========================private=========================private=========================private================================


    private static ZonedDateTime parseDateTimeToZonedDateTime(byte[] timestamp) {
        ByteBuffer buffer = ByteBuffer.wrap(timestamp).order(ByteOrder.BIG_ENDIAN);

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

        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second, totalNanos);
        return localDateTime.atZone(ZoneId.systemDefault());
    }


}
