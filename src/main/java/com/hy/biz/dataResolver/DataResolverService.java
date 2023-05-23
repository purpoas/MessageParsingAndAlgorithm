package com.hy.biz.dataResolver;

import com.hy.biz.dataResolver.dto.BaseMessage;
import com.hy.biz.dataResolver.parser.MessageParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.hy.biz.dataResolver.constants.MessageConstants.EMPTY_MESSAGE_ERROR;


@Component
public class DataResolverService {

    private final MessageParser messageParser;

    public DataResolverService(MessageParser messageParser) {
        this.messageParser = messageParser;
    }

    /**
     * @description 报文解析统一入口函数
     * @param message  16进制字符串
     * @return      解析后的报文数据类型
     */
    public BaseMessage resolve(String message) {
        if (StringUtils.isBlank(message)) throw new IllegalArgumentException(EMPTY_MESSAGE_ERROR);
        return messageParser.parse(message);
    }

}
