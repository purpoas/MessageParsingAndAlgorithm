package com.hy;

import com.hy.biz.parser.MessageParser;
import com.hy.biz.redis.consumer.impl.RedisConsumerImpl;
import com.hy.biz.redis.task.TaskQueue;
import com.hy.biz.redis.task.TaskWorker;
import com.hy.config.HyConfigProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

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
    private final MessageParser messageParser;
    private final RedisTemplate<String, String> redisTemplate;
    private final HyConfigProperty hyConfigProperty;

    public AppStartUp(TaskQueue taskQueue, TaskWorker taskWorker, MessageParser messageParser, RedisTemplate<String, String> redisTemplate, HyConfigProperty hyConfigProperty) {
        this.taskQueue = taskQueue;
        this.taskWorker = taskWorker;
        this.messageParser = messageParser;
        this.redisTemplate = redisTemplate;
        this.hyConfigProperty = hyConfigProperty;
    }

    @Override
    public void onApplicationEvent(@Nonnull ContextRefreshedEvent event) {
        //启动消费线程
        initRedisConsumer();
        //初始化工作线程
        initWorkingThread();
    }

    private void initRedisConsumer() {
        new RedisConsumerImpl(messageParser, taskQueue, hyConfigProperty.getDataQueue().getIddsData(), hyConfigProperty.getDataQueue().getIddsDataBak(), redisTemplate).start();
    }

    private void initWorkingThread() {
        taskWorker.setTaskQueue(taskQueue);
        taskWorker.start();
    }

}

