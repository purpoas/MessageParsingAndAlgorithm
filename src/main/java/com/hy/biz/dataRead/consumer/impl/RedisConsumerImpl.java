package com.hy.biz.dataRead.consumer.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hy.biz.dataPush.task.TaskFactory;
import com.hy.biz.dataPush.task.TaskQueue;
import com.hy.biz.dataRead.consumer.RedisConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PreDestroy;
import java.util.concurrent.ThreadFactory;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.consumer.impl
 * @description
 * @create 2023-04-26 13:53
 **/
@Slf4j
public class RedisConsumerImpl extends RedisConsumer {

    public RedisConsumerImpl(TaskQueue taskQueue, TaskFactory taskFactory, String workingQueue, String backupQueue, int queueCapacity, RedisTemplate<String, String> redisTemplate) {
        super(taskQueue, taskFactory, workingQueue, backupQueue, queueCapacity, redisTemplate);
    }

    public void executeCommand() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Idds-redis-consumer").build();
        super.executeCommand(threadFactory);
    }

    @PreDestroy
    protected void onShutDown() {
        super.stop();
    }


}
