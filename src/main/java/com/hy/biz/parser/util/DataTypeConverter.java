package com.hy.biz.parser.util;

import java.nio.ByteBuffer;

/**
 * @package com.hy.idds.biz.parser.util
 * @description
 * @author shiwentao
 * @create 2023-04-23 17:16
 **/
public class DataTypeConverter {

    public static int byteToInt(byte b) {
        return b & 0xFF;
    }

    public static int shortToInt(short inputShort) {
        int i0 = (inputShort & 0xFF00) >> 8;
        int i1 = inputShort & 0x00FF;
        return (i0 & 0xFF) + ((i1 & 0xFF) << 8);
    }

    public static float shortToFloat(short inputShort) {
        byte[] bytes = new byte[4];
        bytes[0] = 0;
        bytes[1] = 0;
        bytes[2] = (byte) ((inputShort & 0xFF00) >> 8);
        bytes[3] = (byte) (inputShort & 0x00FF);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getFloat();
    }

    public static int floatToInt(float inputFloat) {
        return Float.floatToIntBits(inputFloat);
    }

    public static long shortToLong(short val) {
        return val & 0xFFFFL;
    }

    public static byte[] hexStringToByteArray(String data) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length() / 2);

        for (int i = 0; i < data.length(); i += 2) {
            buffer.put((byte) ((Character.digit(data.charAt(i), 16) << 4) + Character.digit(data.charAt(i + 1), 16)));
        }

        return buffer.array();
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        char[] hexChars = new char[byteArray.length * 2];
        char[] hexDigits = "0123456789ABCDEF".toCharArray();

        for (int i = 0; i < byteArray.length; i++) {
            int byteValue = byteArray[i] & 0xFF;
            hexChars[i * 2] = hexDigits[byteValue >>> 4];
            hexChars[i * 2 + 1] = hexDigits[byteValue & 0x0F];
        }

        return new String(hexChars);
    }

    public static String floatToHexString(float value) {
        int intBits = Float.floatToIntBits(value);
        return String.format("%08X", intBits);
    }



}
