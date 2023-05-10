package com.hy;

import com.hy.biz.dataRead.DataReadService;
import com.hy.biz.redis.task.TaskWorker;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
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
    private final DataReadService dataReadService;

    public AppStartUp(TaskWorker taskWorker, DataReadService dataReadService) {
        this.taskWorker = taskWorker;
        this.dataReadService = dataReadService;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        // 初始化消费者
        initConsumer();
        // 初始化工作线程
        initTaskWorker();
    }

    private void initConsumer() {
        dataReadService.initConsumer();
    }

    private void initTaskWorker() {
        taskWorker.executeTask();
    }
}

