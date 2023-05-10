package com.hy.biz.redis.task.impl;

import com.hy.biz.parser.MessageParser;
import com.hy.biz.parser.entity.dto.MessageDTO;
import com.hy.biz.redis.task.Task;
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
        // TODO 1.调用数据解析模块方法,返回基础数据类型BaseMessage,解析过程中有需要封装实体类中deviceId属性，需要调用DataPushService中findDeviceByCode方法获取deviceId信息
        // TODO 2.根据基础数据类型BaseMessage，调用数据推送模块方法进行推送
        // TODO 3.判断是否波形调用智能算法分析模块方法，进行波形预处理、故障波形判断等操作

        Object entitySaved = messageParser.parse(messageDTO.getTimeStamp(),
                messageDTO.getData().getCommand(),
                messageDTO.getDeviceCode());
        if (entitySaved != null)
            removeFromRedisList(redisTemplate, dataBakQueue);
    }

}
