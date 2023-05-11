package com.hy.biz.dataRead.consumer.impl;

import com.hy.biz.dataRead.consumer.RedisConsumer;
import com.hy.biz.dataRead.task.TaskFactory;
import com.hy.biz.dataRead.task.TaskQueue;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.consumer.impl
 * @description
 * @create 2023-04-26 13:53
 **/
public class RedisConsumerImpl extends RedisConsumer {

    public RedisConsumerImpl(TaskQueue taskQueue, TaskFactory taskFactory, String workingQueue, String backupQueue, int queueCapacity, RedisTemplate<String, String> redisTemplate) {
        super(taskQueue, taskFactory, workingQueue, backupQueue, queueCapacity, redisTemplate);
    }

    public void executeCommand() {
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        super.executeCommand(threadFactory);
    }

    @PreDestroy
    protected void beanDestroy() {
        super.stop();
    }

}
