package com.hy.web;

import com.hy.service.HeartBeatMsgService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shiwentao
 * @package com.hy.web
 * @description
 * @create 2023-04-26 09:15
 **/
@RestController
@RequestMapping("/api")
public class HeartBeatController {
    private final HeartBeatMsgService heartBeatMsgService;

    public HeartBeatController(HeartBeatMsgService heartBeatMsgService) {
        this.heartBeatMsgService = heartBeatMsgService;
    }

    @PostMapping("/heart-beat")
    public String heartBeat() {
        return heartBeatMsgService.toString();
    }
}
