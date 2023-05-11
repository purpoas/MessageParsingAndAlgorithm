package com.hy.biz.dataResolver;

import com.hy.biz.dataResolver.entity.BaseMessage;
import com.hy.biz.dataResolver.parser.MessageParser;
import org.springframework.stereotype.Component;


@Component
public class DataResolverService {

    private final MessageParser messageParser;

    public DataResolverService(MessageParser messageParser) {
        this.messageParser = messageParser;
    }

    /**
     * @description 报文解析统一入口函数
     * @param data  16进制字符串
     * @return      解析后的报文数据类型
     */
    public BaseMessage resolve(String data) {
        return messageParser.parse(data);
    }

}
