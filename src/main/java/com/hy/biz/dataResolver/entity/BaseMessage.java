package com.hy.biz.dataResolver.entity;

import lombok.Data;

/**
 * @package com.hy.idds.biz.dataResolver.MessageEntity
 * @description 报文基类
 * @author shiwentao
 * @create 2023-04-18 17:24
 **/
@Data
public abstract class BaseMessage {
    private byte frameType;
    private byte messageType;
}

