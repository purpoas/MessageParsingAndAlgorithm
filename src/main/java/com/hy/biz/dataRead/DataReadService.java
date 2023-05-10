package com.hy.biz.dataRead;

import com.hy.biz.redis.consumer.impl.RedisConsumerImpl;
import com.hy.biz.redis.task.TaskFactory;
import com.hy.biz.redis.task.TaskQueue;
import com.hy.config.HyConfigProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 消费者实现类
 */
@Component
public class DataReadService {

    private final TaskQueue taskQueue;
    private final TaskFactory taskFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final HyConfigProperty hyConfigProperty;


    public DataReadService(TaskQueue taskQueue, TaskFactory taskFactory, RedisTemplate<String, String> redisTemplate, HyConfigProperty hyConfigProperty) {
        this.taskQueue = taskQueue;
        this.taskFactory = taskFactory;
        this.redisTemplate = redisTemplate;
        this.hyConfigProperty = hyConfigProperty;
    }

    public void initConsumer() {

        // 队列消费模式
        String consumerMode = hyConfigProperty.getDataRead().getMode();

        // TODO 根据配置实现不同数据源获取方式 eg：Redis 、 mysql 、 mqtt等
        switch (consumerMode) {
            case "REDISMQ":
                new RedisConsumerImpl(taskQueue, taskFactory, hyConfigProperty.getDataQueue().getDnmData(), hyConfigProperty.getDataQueue().getDnmDataBak(), hyConfigProperty.getDataQueue().getQueueCapacity(), redisTemplate)
                        .executeCommand();
                break;
            default:
                break;
        }


    }
}
