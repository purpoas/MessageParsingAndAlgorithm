package com.hy;

import com.hy.biz.parser.MessageParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class AppTest {

    @Autowired
    private MessageParser parser;

    @Test
    @Transactional
    @Commit
    void contextLoads() {

        String data =
                "556648595F5057475A5F303030303030303032010102120200005E005E005D005C005C005C005C005C005C005C005C005C005C005C005B005B005B005B005B005B005B005C005C005D005D005D005E005E005E005E005F005F005F005F005F005F005E005E005D005C005B005B005A005A005A005A005B005B005C005C005C005C005C005D005D005D005E005E005E005E005E005E005D005D005D005D005C005D005D005D005D005D005D005C005C005B005B005B005B005C005D005E005E005E005E005E005D005D005D005D005D005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005F005F005F005F005E005E005E005D005D005D005C005C005D005D005D005E005E005D005D005D005D005D005D005E005F005F00600060005F005E005D005D005C005C005C005D005D005E005F0060006000600060005F005F005E005D005C005C005B005B005B005B005B005B005C005C005D005E005E005F005F005F005F005F005E005E005E005E005E005E005E005E005E005D005D005D005D005D005D005E005E005F005F005F005F005F005E005E005E005D005D005D005D005D005D005D005D005E005E005E005E005E005E005E005E005E005E005D005D005D005D005D005E005E005F005F005F005F005E005E005D005D005D005C005C005D005D005D005D005D005D005D005D005D005D005D005D005D005E005E005E005E16070C0800000000000000000F1A010862D9556648595F5057475A5F303030303030303032010102120200005E005E005D005D005C005C005B005B005B005B005B005B005B005C005C005C005D005D005D005E005E005D005D005D005D005D005D005D005E005E005E005D005D005D005C005C005C005B005B005C005C005C005D005E005E005E005E005E005E005E005E005E005F005F005E005E005E005D005D005D005D005C005C005C005D005D005D005E005E005E005D005D005C005C005B005B005C005D005D005E005F005F005F005F005F005F005F005F005F006000600060006000600060005F005F005F005F005F005E005E005E005E005E005E005E005E005E005D005D005C005C005B005B005B005B005B005C005D005E005E005F005F005F005F005F005F00600060005F005F005F005E005E005E005D005D005D005D005D005C005C005D005D005D005D005E005E005F005F005F005F005F005F005F005F005F005F0060006000600060006000600060006000600060005F005E005E005D005C005C005C005C005C005D005D005D005C005C005B005B005B005B005C005D005E005E005F005F005E005E005D005C005C005C005C005C005D005E005E005F005F00600060006000600060006000600060006000600060005F005F005E005E005D005D005D005D005D005D005E005E005E005E005E005F005F005F005F00600060005F005F005E005E005D005D005C005C005C005C005B005B005B005C005C005C005C005C16070C0800000000000000000F1A0208632B556648595F5057475A5F303030303030303032010102120200005B005B005B005B005C005C005D005D005D005D005D005C005C005C005B005B005B005B005C005C005C005D005D005E005E005F005F005F005F005F005E005E005D005D005D005C005C005C005C005C005C005C005C005D005D005E005E005E005F005F005F005E005E005E005E005E005E005E005F005F005E005E005D005D005D005D005D005E005F005F005F005E005E005C005B005B005B005B005C005D005F0060006000600060005F005E005D005C005C005C005C005D005E005F0060006000610061006000600060005F005E005E005D005D005C005C005D005D005D005E005F005F006000600060006000600060005F005F005F005E005E005E005E005E005E005F005F005F005F005F005F006000600060005F005F005E005E005D005D005D005D005D005D005D005D005D005C005C005C005C005D005D005D005D005D005D005D005D005D005D005D005D005D005E005E005E005F005F005F005F005E005E005E005E005E005E005F005F005F006000600060006000600060006000600060005F005F005E005E005D005D005D005D005D005D005D005D005D005D005C005C005B005B005B005B005B005C005C005D005D005E005F005F006000600060006000600060005F005E005E005E005D005D005D005D005D005D005D005D005D005D005D005D005D005D005D005D005D005D005D005C005C005C005C005C16070C0800000000000000000F1A03086338556648595F5057475A5F303030303030303032010102120200005C005C005D005D005D005D005E005E005D005D005E005E005E005F005F005F005E005E005D005D005C005C005C005C005D005D005D005D005D005D005C005C005C005B005B005B005C005C005C005D005D005D005D005D005C005C005C005C005B005B005B005B005B005C005C005C005C005C005C005C005C005D005E005F005F00600060005F005F005E005D005C005C005C005D005E005F006000600060006100600060005F005F005E005D005C005B005A005A005A005B005C005C005D005D005D005C005C005B005B005B005B005B005B005C005D005E005F006000600060005F005F005E005E005E005E005E005F005F005E005E005E005E005D005E005E005E005E005F005F005F005F005F005F005F005F005F00600060006000600060005F005F005E005D005D005C005B005B005B005B005B005C005C005C005C005C005B005B005B005B005C005C005D005D005E005E005E005D005D005C005C005C005B005B005C005C005C005D005D005D005D005D005D005C005B005B005B005B005B005B005B005B005C005C005C005D005D005C005C005C005C005B005B005B005B005B005B005C005C005C005D005D005D005D005D005D005C005C005C005C005C005C005D005D005E005E005D005D005C005B005B005B005B005C005C005D005E005E005E005E005E005D005D005C005C005C005C005C005C005C005C16070C0800000000000000000F1A0408626D556648595F5057475A5F303030303030303032010102120200005B005B005C005C005C005D005D005D005D005D005D005D005D005C005C005C005C005D005D005E005E005E005E005D005C005C005B005B005B005B005B005C005D005E005F005F006000600060006000600060006000600060005F005F005F005F005F005F005F00600060006000600060005F005F005E005E005D005E005E005F005F005F0060005F005F005F005E005E005E005F005F005E005E005E005E005D005E005E005E005E005E005E005D005D005C005C005C005C005C005C005C005C005C005C005C005C005C005C005D005D005D005D005E005E005E005E005D005D005D005C005C005C005C005C005C005C005C005C005C005D005D005D005D005D005D005C005C005C005C005D005D005E005F005F005F005F005E005E005D005C005C005C005C005D005D005E005E005E005E005E005E005E005E005E005E005E005D005D005D005D005D005C005D005D005D005D005D005D005D005D005E005E005E005E005E005E005E005E005E005E005E005E005E005E005E005D005D005C005C005C005C005D005E005E005E005E005E005D005D005C005C005C005C005D005E005E005F005F005F005F005F005E005E005D005D005D005D005D005D005C005C005B005B005B005B005B005C005C005D005D005D005D005D005D005D005C005C005C005C005C005C005D005D005D005D005D005D005D005D005D005C16070C0800000000000000000F1A050862E2556648595F5057475A5F303030303030303032010102120200005C005D005D005D005E005E005E005E005F005F005F005F005F005F005F005E005D005D005C005C005C005C005C005C005D005E005E005F005F005F005F005F005E005E005D005D005D005D005E005E005E005E005E005E005F005F005F005F005F005F005E005E005D005C005B005B005B005B005B005B005B005B005B005B005B005B005C005D005E005F005F005F005F005E005D005D005D005D005D005D005E005E005E005E005E005E005E005E005E005E005E005E005E005D005D005D005D005D005D005E005F005F006000600060005F005F005E005D005D005D005C005C005D005D005D005D005D005D005C005C005D005D005D005D005D005D005D005D005D005C005C005D005D005D005D005D005D005D005D005D005D005E005E005E005E005E005E005E005D005D005C005C005B005B005C005C005C005C005D005D005D005E005E005F005F005F005F005F005F005F005E005E005E005D005D005C005C005B005B005B005B005C005D005E006000600061006100600060005F005E005E005E005F005F006000600060005F005F005E005E005E005D005D005D005D005D005D005D005D005D005C005C005C005C005C005C005C005D005D005E005E005E005E005E005E005D005D005D005D005D005D005D005D005D005D005D005D005D005D005D005D005E005E005E005E005D005D005C005B005B005B005B16070C0800000000000000000F1A060862F9556648595F5057475A5F303030303030303032010102120200005B005B005C005C005C005C005C005C005C005C005C005C005C005B005B005B005B005C005C005D005D005E005E005E005E005E005E005E005E005F005F005F005F005F005F005E005E005E005E005E005D005D005D005D005C005C005C005B005B005B005B005B005C005C005D005D005D005E005E005E005E005E005E005D005D005C005C005C005B005B005C005C005D005D005D005E005E005E005E005D005D005C005B005B005B005B005B005C005C005C005D005D005D005C005C005C005C005C005D005D005D005D005D005E005E005E005E005E005E005E005E005E005D005D005C005C005C005C005D005D005E005F005F005F005F005F005F005F005F005F005E005E005E005E005D005D005D005D005D005E005E005E005F005F00600060006000600060005F005E005D005D005C005B005B005B005B005B005B005B005C005C005D005D005D005D005E005E005E005E005E005E005E005E005E005E005F005F005F005F005F005F005E005D005C005C005B005B005B005B005B005B005B005B005B005B005B005B005B005B005C005C005D005E005E005E005F005E005E005E005E005E005E005E005E005E005E005E005D005D005D005C005B005B005A005A005A005B005C005D005D005D005D005D005C005C005C005C005C005C005C005C005C005C005C005D005D005D005D005E005E005E005E005D005D16070C0800000000000000000F1A07086287556648595F5057475A5F3030303030303030320101009F008D005D005D005D005D005D005D005D005E005E005E005E005E005E005E005E005F005F005E005E005E005D005C005C005C005C005C005C005C005C005C005C005D005D005E005F005F005F005F005F005E005D005C005B005B005B005B005C005D005E005F005F006000600060005F005F005E005D005C005C005C005C005C005C005C005D005D005D005D005C0016070C0800000000000000000F1A0808202C";
        //[0, 94, 0, 94, 0, 93, 0, 92, 0, 92, 0, 92, 0, 92, 0, 92, 0, 92, 0, 92, 0, 92, 0, 92, 0, 92, 0, 92, 0, 91, 0, 91, 0, 91, 0, 91, 0, 91, 0, 91, 0, 91, 0, 92, 0, 92, 0, 93, 0, 93, 0, 93, 0, 94, 0, 94, 0, 94, 0, 94, 0, 95, 0, 95, 0, 95, 0, 95, 0, 95, 0, 95, 0, 94, 0, 94, 0, 93, 0, 92, 0, 91, 0, 91, 0, 90, 0, 90, 0, 90, 0, 90, 0, 91, 0, 91, 0, 92, 0, 92, 0, 92, 0, 92, 0, 92, 0, 93, 0, 93, 0, 93, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 93, 0, 93, 0, 93, 0, 93, 0, 92, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 92, 0, 92, 0, 91, 0, 91, 0, 91, 0, 91, 0, 92, 0, 93, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 95, 0, 95, 0, 95, 0, 95, 0, 94, 0, 94, 0, 94, 0, 93, 0, 93, 0, 93, 0, 92, 0, 92, 0, 93, 0, 93, 0, 93, 0, 94, 0, 94, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 94, 0, 95, 0, 95, 0, 96, 0, 96, 0, 95, 0, 94, 0, 93, 0, 93, 0, 92, 0, 92, 0, 92, 0, 93, 0, 93, 0, 94, 0, 95, 0, 96, 0, 96, 0, 96, 0, 96, 0, 95, 0, 95, 0, 94, 0, 93, 0, 92, 0, 92, 0, 91, 0, 91, 0, 91, 0, 91, 0, 91, 0, 91, 0, 92, 0, 92, 0, 93, 0, 94, 0, 94, 0, 95, 0, 95, 0, 95, 0, 95, 0, 95, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 94, 0, 94, 0, 95, 0, 95, 0, 95, 0, 95, 0, 95, 0, 94, 0, 94, 0, 94, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 94, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 94, 0, 94, 0, 95, 0, 95, 0, 95, 0, 95, 0, 94, 0, 94, 0, 93, 0, 93, 0, 93, 0, 92, 0, 92, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 93, 0, 94, 0, 94, 0, 94, 0, 94]
        String deviceCode = "HY_PWGZ_000000002";
        long timeStamp = 1682661160266L;

        parser.parse(timeStamp, data, deviceCode);

    }

}
