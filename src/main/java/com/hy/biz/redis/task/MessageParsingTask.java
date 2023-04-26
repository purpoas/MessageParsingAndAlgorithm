package com.hy.biz.redis.task;

import com.hy.biz.parser.MessageParser;
import com.hy.biz.parser.entity.dto.MessageDTO;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @description
 * @create 2023-04-26 14:59
 **/
public class MessageParsingTask extends Task {
    private final MessageParser messageParser;
    private final MessageDTO messageDTO;

    public MessageParsingTask(MessageParser messageParser, MessageDTO messageDTO) {
        this.messageParser = messageParser;
        this.messageDTO = messageDTO;
    }

    @Override
    public void run() {
        messageParser.parse(messageDTO.getDateTime(), messageDTO.getData().getCommand(), messageDTO.getDeviceCode());
    }
}
