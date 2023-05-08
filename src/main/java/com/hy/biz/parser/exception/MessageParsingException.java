package com.hy.biz.parser.exception;

/**
 * @package com.hy.idds.biz.parser.exception
 * @description
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

