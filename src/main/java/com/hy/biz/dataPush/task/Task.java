package com.hy.biz.dataPush.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @create 2023-04-26 10:54
 **/
public abstract class Task implements Runnable {

    /**
     * Redis 消息队列中接收到的 JSON 数据
     */
    protected String message;

    /**
     *
     * 将处理成功的消息从消息队列中移除
     *
     * @param redisTemplate redisTemplate
     * @param backupQueue backupQueue
     */
    protected void removeFromRedisList(RedisTemplate<String, String> redisTemplate, String backupQueue) {
        if (StringUtils.isNotBlank(backupQueue)) {
            redisTemplate.opsForList().remove(backupQueue, 1, this.message);
        }
    }


}
