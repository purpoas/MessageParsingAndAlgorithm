package com.hy.biz.parser.entity;

import lombok.Data;

/**
 * @package com.hy.idds.biz.parser.MessageEntity
 * @description
 * @author shiwentao
 * @create 2023-04-18 17:24
 **/
@Data
public abstract class BaseMessage {
    private byte[] idNumber;
    private byte messageType;
}

