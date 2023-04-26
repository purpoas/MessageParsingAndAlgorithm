package com.hy.biz.redis.consumer.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hy.biz.parser.MessageParser;
import com.hy.biz.redis.consumer.RedisConsumer;
import com.hy.biz.redis.task.TaskQueue;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PreDestroy;
import java.util.concurrent.ThreadFactory;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.consumer.impl
 * @description
 * @create 2023-04-26 13:53
 **/
public class RedisConsumerImpl extends RedisConsumer {

    public RedisConsumerImpl(MessageParser messageParser, TaskQueue taskQueue, String workingQueue, String backupQueue, RedisTemplate<String, String> redisTemplate) {
        super(messageParser, taskQueue, workingQueue, backupQueue, redisTemplate);
    }

    public void start() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("redis-consumer-dfm").build();
        super.start(threadFactory);
    }

    @PreDestroy
    protected void beanDestroy() {
        super.stop();
    }

}
