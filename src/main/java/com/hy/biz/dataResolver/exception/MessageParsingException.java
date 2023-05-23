package com.hy.biz.dataResolver.exception;

/**
 *
 * 解析异常类
 *
 * @package com.hy.idds.biz.dataResolver.exception
 * @author shiwentao
 * @create 2023-04-19 13:34
 **/
public class MessageParsingException extends RuntimeException {

    public MessageParsingException(String message) {
        super(message);
    }

    public MessageParsingException(String message, Throwable cause) {
        super(message, cause);
    }


}

