package com.hy.biz.dataParsing.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 *
 * 数据类型转换工具类，主要负责 byte数组 与 String 类型之间的切换
 *
 * @package com.hy.idds.biz.dataParsing.algorithmUtil
 * @author shiwentao
 * @create 2023-04-23 17:16
 **/
public class TypeConverter {

    public static byte[] hexStringToByteArray(String data) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length() / 2);

        for (int i = 0; i < data.length(); i += 2) {
            buffer.put((byte) ((Character.digit(data.charAt(i), 16) << 4) + Character.digit(data.charAt(i + 1), 16)));
        }

        return buffer.array();
    }

    public static String byteArrToStr(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.UTF_8);
    }


}
