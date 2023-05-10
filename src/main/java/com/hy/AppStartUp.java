package com.hy;

import com.hy.biz.dataRead.IddsDataConsumer;
import com.hy.biz.redis.task.TaskFactory;
import com.hy.biz.redis.task.TaskQueue;
import com.hy.biz.redis.task.TaskWorker;
import com.hy.config.HyConfigProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy
 * @description
 * @create 2023-04-26 10:43
 **/
@Component
public class AppStartUp implements ApplicationListener<ContextRefreshedEvent> {
    private final TaskWorker taskWorker;
    private final IddsDataConsumer iddsDataConsumer;

    public AppStartUp(TaskWorker taskWorker, IddsDataConsumer iddsDataConsumer) {
        this.taskWorker = taskWorker;
        this.iddsDataConsumer = iddsDataConsumer;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        // 初始化消费者
        initConsumer();
        // 初始化工作线程
        initTaskWorker();
    }

    private void initConsumer() {
        iddsDataConsumer.initConsumer();
    }

    private void initTaskWorker() {
        taskWorker.executeTask();
    }
}

