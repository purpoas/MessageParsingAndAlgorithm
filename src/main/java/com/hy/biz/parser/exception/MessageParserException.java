package com.hy.biz.parser.exception;

/**
 * @package com.hy.idds.biz.parser.exception
 * @description
 * @author shiwentao
 * @create 2023-04-19 13:34
 **/
public class MessageParserException extends RuntimeException {
    public MessageParserException(String message) {
        super(message);
    }

    public MessageParserException(String message, Throwable cause) {
        super(message, cause);
    }
}

