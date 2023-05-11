package com.hy;

import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.biz.dataResolver.DataResolverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class AppTest {

    @Autowired
    private DataPushService dataPushService;

    @Autowired
    private DataResolverService dataResolverService;

    @Test
    @Transactional
    @Commit
    void contextLoads() {

        String jsonString = "{\"dateTime\": 1682058706392, \"data\": { \"command\": \"556648595F5057475A5F303030303030303032050A002D1704120A0A07000000000000000001A81B4F001B1B4F00020F065E060F01A8000000000050000000000000000008B3\"}, \"deviceCode\": \"HY_PWGZ_000000002\"}";

        dataPushService.push(jsonString, dataResolverService.resolve(jsonString), PushDataType.DEVICE_STATUS);

    }

}
