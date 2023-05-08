package com.hy.biz.redis.task;

import com.hy.biz.parser.MessageParser;
import com.hy.biz.parser.entity.dto.MessageDTO;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @description
 * @create 2023-04-26 14:59
 **/
public class MessageParsingTask extends Task {
    private final MessageParser messageParser;
    private final MessageDTO messageDTO;
    private final RedisTemplate<String, String> redisTemplate;
    private final String dataBakQueue;

    public MessageParsingTask(String message, MessageDTO messageDTO, MessageParser messageParser, RedisTemplate<String, String> redisTemplate, String dataBakQueue) {
        this.message = message;
        this.messageDTO = messageDTO;
        this.messageParser = messageParser;
        this.redisTemplate = redisTemplate;
        this.dataBakQueue = dataBakQueue;
    }

    @Override
    public void run() {
        boolean flag = messageParser.parse(messageDTO.getTimeStamp(),
                messageDTO.getData().getCommand(),
                messageDTO.getDeviceCode());
        if (!flag)
            removeFromRedisList(redisTemplate, dataBakQueue);
    }

}
