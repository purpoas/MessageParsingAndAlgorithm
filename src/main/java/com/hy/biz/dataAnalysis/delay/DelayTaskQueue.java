package com.hy.biz.dataAnalysis.delay;

import com.hy.biz.dataAnalysis.dto.AlgorithmIdentify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;

/**
 * 延迟任务队列, 队列中任务为算法任务唯一标识
 */
@Component
public class DelayTaskQueue {
    private final Logger log = LoggerFactory.getLogger(DelayTaskQueue.class);

    // 延迟队列
    private final DelayQueue<AlgorithmIdentify> queue;

    public DelayTaskQueue() {
        queue = new DelayQueue<>();
    }

    /**
     * 添加新元素到队列中
     *
     * @param identify
     */
    public void add(AlgorithmIdentify identify) {
        if (identify != null) {
            queue.add(identify);
        }
    }

    /**
     * 添加新任务到队列中
     *
     * @param identify
     * @throws InterruptedException
     */
    public void put(AlgorithmIdentify identify) {
        if (identify != null) {
            queue.put(identify);
        }
    }

    /**
     * 从队列中获取待处理任务
     *
     * @return
     * @throws InterruptedException
     */
    public AlgorithmIdentify take() throws InterruptedException {
        return queue.take();
    }

    /**
     * 返回当前任务队列所含任务的数目
     *
     * @return
     */
    public int size() {
        return queue.size();
    }
}
