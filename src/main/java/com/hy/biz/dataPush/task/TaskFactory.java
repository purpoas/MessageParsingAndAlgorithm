package com.hy.biz.dataPush.task;

import com.hy.biz.dataPush.DataPushService;
import com.hy.biz.dataPush.task.impl.MessageParsingTask;
import com.hy.biz.dataResolver.DataResolverService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * 任务工厂
 *
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @create 2023-04-27 15:58
 **/
@Component
public class TaskFactory {

    private final RedisTemplate<String, String> redisTemplate;
    private final DataResolverService dataResolverService;
    private final DataPushService dataPushService;

    public TaskFactory(RedisTemplate<String, String> redisTemplate, DataResolverService dataResolverService, DataPushService dataPushService) {
        this.redisTemplate = redisTemplate;
        this.dataResolverService = dataResolverService;
        this.dataPushService = dataPushService;
    }

    public Task createTask(String message, String backupQueue) {
        return new MessageParsingTask(message, redisTemplate, backupQueue, dataResolverService, dataPushService);
    }

}
