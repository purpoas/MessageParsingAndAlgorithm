package com.hy.biz.redis.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @description
 * @create 2023-04-26 14:23
 **/
@Component
public class TaskWorker {
    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;
    private TaskQueue taskQueue;
    private ExecutorService executorService;
    private volatile boolean stopThread = false;

    public TaskWorker() {
        super();
    }

    public TaskWorker(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void setTaskQueue(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    /**
     * 启动任务队列工作线程
     */
    public void start() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("base-data-task-worker").build();
        executorService = Executors.newSingleThreadExecutor(threadFactory);
        executorService.execute(this::process);
        executorService.shutdown();
    }

    /**
     * 业务处理
     */
    private void process() {

        Task lastTask = null;

        while (!stopThread) {
            // 线程停止标志置位
            Task task = null;
            try {
                if (lastTask != null) {
                    task = lastTask;
                    TimeUnit.SECONDS.sleep(10L);
                } else {
                    task = taskQueue.takeTask();
                }

                if (task != null) {
                    lastTask = null;
                    taskExecutor.execute(task);
                }
            } catch (RejectedExecutionException e) {
                lastTask = task;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 程序停止时, 设置业务处理停止标志, 并关闭线程.
     */
    @PreDestroy
    public void beanDestroy() {
        this.stopThread = true;
        if (executorService != null) {
            try {
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

}
