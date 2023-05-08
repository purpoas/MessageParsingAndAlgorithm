package com.hy.biz.parser.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @package com.hy.idds.biz.parser.MessageEntity
 * @description
 * @author shiwentao
 * @create 2023-04-13 13:49
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class HeartBeatMessage extends BaseMessage {}
