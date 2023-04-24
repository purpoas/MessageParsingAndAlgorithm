package com.hy;

import com.hy.biz.MessageParsing.MessageParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;

@SpringBootTest
class AppTest {
    private final MessageParser parser;

    AppTest(MessageParser parser) {
        this.parser = parser;
    }

    @Test
    @Transactional
    @Commit
    void contextLoads() {

        String data = "556648595F5057475A5F303030303030303032050A002D1704120A0A07000000000000000001A81B4F001B1B4F00020F065E060F01A8000000000050000000000000000008B3";
        String deviceCode = "HY_PWGZ_000000002";
        long value = 1682058706392L;
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        byte[] dateTime = buffer.array();

        parser.parse(dateTime, data, deviceCode);
    }

}
