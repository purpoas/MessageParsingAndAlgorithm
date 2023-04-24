package com.hy.biz.MessageParsing.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @package: com.hy.idds.biz.MessageParsing.util
 * @description:
 * @author: shiwentao
 * @create: 2023-04-23 17:16
 **/
public class DataTypeConverter {
    public static byte[] hexStringToByteArray(String data) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length() / 2);

        for (int i = 0; i < data.length(); i += 2) {
            buffer.put((byte) ((Character.digit(data.charAt(i), 16) << 4) + Character.digit(data.charAt(i + 1), 16)));
        }

        return buffer.array();
    }

    public static String byteArrayToString(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.UTF_8);
    }
}
