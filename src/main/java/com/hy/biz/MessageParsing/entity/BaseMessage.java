package com.hy.biz.MessageParsing.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @package com.hy.idds.biz.MessageParsing.MessageEntity
 * @description
 * @author shiwentao
 * @create 2023-04-18 17:24
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseMessage {
    private byte[] idNumber;
    private byte messageType;
}

