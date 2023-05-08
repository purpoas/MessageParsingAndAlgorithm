package com.hy.biz.redis.task;

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

    public TaskFactory(MessageParser messageParser, RedisTemplate<String, String> redisTemplate) {
        this.messageParser = messageParser;
        this.redisTemplate = redisTemplate;
    }

    public Task createTask(String message, MessageDTO messageDTO, String backupQueue) {
        return new MessageParsingTask(message, messageDTO, messageParser, redisTemplate, backupQueue);
    }

}
