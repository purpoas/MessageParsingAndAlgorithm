package com.hy.biz.MessageParsing.entity;

import com.hy.biz.MessageParsing.util.GSONUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @package com.hy.idds.biz.MessageParsing.MessageEntity
 * @description
 * @author shiwentao
 * @create 2023-04-13 13:49
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class HeartBeatMessage extends BaseMessage {

    @Override
    public String toString() {
        return GSONUtil.getInstance().toJson(this);
    }

}
