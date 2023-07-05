package com.hy.biz.dataPush.task.impl;

import com.hy.biz.dataAnalysis.DataAnalysisService;
import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.biz.dataPush.task.Task;
import com.hy.biz.dataParsing.DataParserService;
import com.hy.biz.dataParsing.dto.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.hy.biz.dataPush.dto.PushDataType.*;

/**
 * ========================================
 * 报文解析任务（负责阻塞队列中的报文数据）       ｜
 * ========================================
 *
 * @author shiwentao
 * @package com.hy.biz.dataPush.task.impl
 * @create 2023-05-23 09:27
 **/
public class MessageParsingTask extends Task {

    private static final Map<Class<? extends BaseMessage>, PushDataType> messageTypeMap = new HashMap<>();

    static {
        messageTypeMap.put(DeviceFaultMessage.class, DEVICE_FAULT);
        messageTypeMap.put(DeviceInfoMessage.class, DEVICE_INFO);
        messageTypeMap.put(DeviceStatusMessage.class, DEVICE_STATUS);
        messageTypeMap.put(WorkStatusMessage.class, WORK_STATUS);
        messageTypeMap.put(WaveDataMessage.class, WAVE);
    }

    private final RedisTemplate<String, String> redisTemplate;
    private final String dataBakQueue;
    private final DataParserService dataParserService;
    private final DataPushService dataPushService;
    private final DataAnalysisService dataAnalysisService;

    public MessageParsingTask(String message, RedisTemplate<String, String> redisTemplate, String dataBakQueue,
                              DataParserService dataParserService, DataPushService dataPushService,
                              DataAnalysisService dataAnalysisService) {
        this.message = message;
        this.redisTemplate = redisTemplate;
        this.dataBakQueue = dataBakQueue;
        this.dataParserService = dataParserService;
        this.dataPushService = dataPushService;
        this.dataAnalysisService = dataAnalysisService;
    }

    @Override
    public void run() {
        // 数据解析
        BaseMessage baseMessage = dataParserService.parse(message);

        messageTypeMap.forEach((messageClass, pushType) -> {
            if (messageClass.isInstance(baseMessage)) {
                boolean flag = dataPushService. push(message, baseMessage, pushType);

                if (pushType == WAVE) {
                    WaveDataMessage wave = (WaveDataMessage) baseMessage;
                    dataAnalysisService.createAlgorithmTask(wave);
                }

                if (flag) removeFromRedisList(redisTemplate, dataBakQueue);
            }
        });
    }


}
