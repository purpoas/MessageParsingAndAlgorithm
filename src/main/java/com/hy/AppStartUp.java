package com.hy;

import com.hy.biz.redis.consumer.impl.RedisConsumerImpl;
import com.hy.biz.redis.task.TaskFactory;
import com.hy.biz.redis.task.TaskQueue;
import com.hy.biz.redis.task.TaskWorker;
import com.hy.config.HyConfigProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy
 * @description
 * @create 2023-04-26 10:43
 **/
@Component
public class AppStartUp implements ApplicationListener<ContextRefreshedEvent> {
    private final TaskQueue taskQueue;
    private final TaskWorker taskWorker;
    private final TaskFactory taskFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final HyConfigProperty hyConfigProperty;

    public AppStartUp(TaskQueue taskQueue, TaskWorker taskWorker, TaskFactory taskFactory, RedisTemplate<String, String> redisTemplate, HyConfigProperty hyConfigProperty) {
        this.taskQueue = taskQueue;
        this.taskWorker = taskWorker;
        this.taskFactory = taskFactory;
        this.redisTemplate = redisTemplate;
        this.hyConfigProperty = hyConfigProperty;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        initRedisConsumer();
        initTaskWorker();
    }

    private void initRedisConsumer() {
        new RedisConsumerImpl(taskQueue, taskFactory, hyConfigProperty.getDataQueue().getDnmData(), hyConfigProperty.getDataQueue().getDnmDataBak(), hyConfigProperty.getDataQueue().getQueueCapacity(), redisTemplate)
                .executeCommand();
    }

    private void initTaskWorker() {
        taskWorker.executeTask();
    }
}

