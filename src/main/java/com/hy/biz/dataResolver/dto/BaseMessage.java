package com.hy.biz.dataResolver.dto;

import lombok.Data;

/**
 *
 * 报文基类
 *
 * @package com.hy.idds.biz.dataResolver.MessageEntity
 * @author shiwentao
 * @create 2023-04-18 17:24
 **/
@Data
public abstract class BaseMessage {
    private byte frameType;
    private byte messageType;
}

