package com.hy.biz.redis.task;

import com.hy.biz.dataAnalysis.DataAnalysisService;
import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataResolver.DataResolverService;
import com.hy.biz.parser.MessageParser;
import com.hy.biz.parser.entity.dto.MessageDTO;
import com.hy.biz.redis.task.impl.MessageParsingTask;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @description
 * @create 2023-04-27 15:58
 **/
@Component
public class TaskFactory {
    private final MessageParser messageParser;
    private final RedisTemplate<String, String> redisTemplate;
    private final DataResolverService dataResolverService;
    private final DataPushService dataPushService;
    private final DataAnalysisService dataAnalysisService;

    public TaskFactory(MessageParser messageParser, RedisTemplate<String, String> redisTemplate, DataResolverService dataResolverService, DataPushService dataPushService, DataAnalysisService dataAnalysisService) {
        this.messageParser = messageParser;
        this.redisTemplate = redisTemplate;
        this.dataResolverService = dataResolverService;
        this.dataPushService = dataPushService;
        this.dataAnalysisService = dataAnalysisService;

    }

    public Task createTask(String message, MessageDTO messageDTO, String backupQueue) {
        return new MessageParsingTask(message, messageDTO, messageParser, redisTemplate, backupQueue, dataResolverService, dataPushService, dataAnalysisService);
    }

}
