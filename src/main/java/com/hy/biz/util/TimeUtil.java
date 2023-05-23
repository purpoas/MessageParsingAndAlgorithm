package com.hy.biz.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    public Instant test() {
        return null;
    }

    /**
     * 20200403093000123321234 ----> 1585877400123
     * <p>
     * 将所给的字符串转化为毫秒时间戳
     **/
    public static long handleHeadTimeToTimestamp(String headTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssnnnnnnnnn");
        LocalDateTime localDateTime = LocalDateTime.parse(headTime, dateTimeFormatter);
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
