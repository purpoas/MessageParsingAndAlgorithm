package com.hy.biz.dataParsing.dto;

import lombok.Data;

/**
 *
 * 报文基类
 *
 * @package com.hy.idds.biz.dataParsing.MessageEntity
 * @author shiwentao
 * @create 2023-04-18 17:24
 **/
@Data
public abstract class BaseMessage {
    private byte frameType;
    private byte messageType;
    private String deviceCode;
}

