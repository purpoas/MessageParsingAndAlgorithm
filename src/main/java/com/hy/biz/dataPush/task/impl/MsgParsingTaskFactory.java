package com.hy.biz.dataPush.task.impl;

import com.hy.biz.dataAnalysis.DataAnalysisService;
import com.hy.biz.dataParsing.DataParserService;
import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataPush.task.Task;
import com.hy.biz.dataPush.task.TaskFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @create 2023-04-27 15:58
 **/
@Component
public class MsgParsingTaskFactory implements TaskFactory {

    private final RedisTemplate<String, String> redisTemplate;
    private final DataParserService dataParserService;
    private final DataPushService dataPushService;
    private final DataAnalysisService dataAnalysisService;

    public MsgParsingTaskFactory(RedisTemplate<String, String> redisTemplate, DataParserService dataParserService, DataPushService dataPushService, DataAnalysisService dataAnalysisService) {
        this.redisTemplate = redisTemplate;
        this.dataParserService = dataParserService;
        this.dataPushService = dataPushService;
        this.dataAnalysisService = dataAnalysisService;
    }

    @Override
    public Task createTask(String message, String backupQueue) {
        return new MessageParsingTask(message, redisTemplate, backupQueue, dataParserService, dataPushService, dataAnalysisService);
    }


}
