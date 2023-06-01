package com.hy;

import com.hy.biz.dataRead.DataReadService;
import com.hy.biz.dataPush.task.TaskWorker;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author shiwentao
 * @package com.hy
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
        // 初始化 REDIS 消费者线程
        dataReadService.initConsumer();
        // 初始化任务工作线程
        taskWorker.executeTask();
    }

}

