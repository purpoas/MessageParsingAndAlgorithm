package com.hy.biz.dataParsing.exception;

/**
 *==============
 * 解析异常类    ｜
 *==============
 *
 * @package com.hy.idds.biz.dataParsing.exception
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

