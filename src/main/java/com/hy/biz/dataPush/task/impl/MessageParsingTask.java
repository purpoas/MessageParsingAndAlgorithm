package com.hy.biz.dataPush.task.impl;

import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.biz.dataPush.task.Task;
import com.hy.biz.dataResolver.DataResolverService;
import com.hy.biz.dataResolver.entity.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.hy.biz.dataPush.dto.PushDataType.*;

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
    private final DataResolverService dataResolverService;
    private final DataPushService dataPushService;

    public MessageParsingTask(String message, RedisTemplate<String, String> redisTemplate, String dataBakQueue,
                              DataResolverService dataResolverService, DataPushService dataPushService) {
        this.message = message;
        this.redisTemplate = redisTemplate;
        this.dataBakQueue = dataBakQueue;
        this.dataResolverService = dataResolverService;
        this.dataPushService = dataPushService;
    }

    @Override
    public void run() {

        BaseMessage baseMessage = dataResolverService.resolve(message);
        messageTypeMap.forEach((messageClass, pushType) -> {
            if (messageClass.isInstance(baseMessage)) {
                boolean flag = dataPushService.push(message, baseMessage, pushType);
                if (flag) {
                    removeFromRedisList(redisTemplate, dataBakQueue);
                }
            }
        });

    }


}
