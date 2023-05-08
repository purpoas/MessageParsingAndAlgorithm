package com.hy.biz.redis.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @description
 * @create 2023-04-26 10:54
 **/
public abstract class Task implements Runnable {

    protected String message;

    protected void removeFromRedisList(RedisTemplate<String, String> redisTemplate, String backupQueue) {
        if (StringUtils.isNotBlank(backupQueue)) {
            redisTemplate.opsForList().remove(backupQueue, 1, this.message);
        }
    }

}
