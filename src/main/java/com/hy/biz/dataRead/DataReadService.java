package com.hy.biz.dataRead;

import com.hy.biz.dataRead.consumer.impl.RedisConsumerImpl;
import com.hy.biz.dataPush.task.impl.MsgParsingTaskFactory;
import com.hy.biz.dataPush.task.TaskQueue;
import com.hy.config.HyConfigProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据获取
 */
@Component
public class DataReadService {

    private final TaskQueue taskQueue;
    private final MsgParsingTaskFactory msgParsingTaskFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final HyConfigProperty hyConfigProperty;


    public DataReadService(TaskQueue taskQueue, MsgParsingTaskFactory msgParsingTaskFactory, RedisTemplate<String, String> redisTemplate, HyConfigProperty hyConfigProperty) {
        this.taskQueue = taskQueue;
        this.msgParsingTaskFactory = msgParsingTaskFactory;
        this.redisTemplate = redisTemplate;
        this.hyConfigProperty = hyConfigProperty;
    }

    /**
     * 开启 REDIS 消费者线程
     */
    public void initConsumer() {
        // 队列消费模式
        String consumerMode = hyConfigProperty.getDataRead().getMode();

        if ("REDISMQ".equals(consumerMode))
            new RedisConsumerImpl(taskQueue, msgParsingTaskFactory, hyConfigProperty.getDataQueue().getDnmData(), hyConfigProperty.getDataQueue().getDnmDataBak(), hyConfigProperty.getDataQueue().getQueueCapacity(), redisTemplate)
                    .executeCommand();
    }


}
