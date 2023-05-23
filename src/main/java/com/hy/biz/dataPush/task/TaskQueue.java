package com.hy.biz.dataPush.task;

import com.hy.config.HyConfigProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * 自定义消息阻塞队列
 *
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @create 2023-04-26 10:59
 **/
@Component
public class TaskQueue {
    private final ArrayBlockingQueue<Task> blockingQueue;

    public TaskQueue(HyConfigProperty configProperty) {
        blockingQueue = new ArrayBlockingQueue<>(configProperty.getDataQueue().getQueueCapacity());
    }

    /**
     * @description 将一个新任务put到队列中
     * @param task 任务
     */
    public void putTask(Task task) {
        if (task != null) {
            try {
                blockingQueue.put(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @description 从队列中获取待处理任务
     * @return 待处理任务
     * @throws InterruptedException e
     */
    public Task takeTask() throws InterruptedException {
        return blockingQueue.take();
    }

}
