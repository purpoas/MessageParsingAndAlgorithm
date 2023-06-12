package com.hy.biz.dataPush.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @create 2023-04-26 14:23
 **/
@Slf4j
@Component
public class TaskWorker {

    private final TaskQueue taskQueue;
    private final TaskExecutor taskExecutor;

    private volatile boolean threadStopped = false;
    private ExecutorService executorService;

    public TaskWorker(TaskQueue taskQueue, @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.taskQueue = taskQueue;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 启动任务队列工作线程
     */
    public void executeTask() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("idds-task-worker-thread").build();
        executorService = Executors.newSingleThreadExecutor(threadFactory);
        executorService.execute(this::command);
        executorService.shutdown();
    }

    /**
     * 业务处理
     */
    private void command() {
        Task lastTask = null;

        while (!threadStopped) {
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("TaskWorker InterruptedException Error: {}", e.getMessage());
            } catch (RejectedExecutionException e) {
                log.error("Task Queue RejectedExecutionException Error: {}", e.getMessage());
                lastTask = task;
            }
        }
    }

    /**
     * 程序停止时, 允许正在执行的任务有一段缓冲期，之后再关闭线程.
     */
    @PreDestroy
    public void beanDestroy() {
        threadStopped = true;
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.info("TaskWorker thread [{}] shutdown failed", Thread.currentThread().getName());
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.info("RedisConsumer thread [{}] force shutdown failed", Thread.currentThread().getName());
                    } else {
                        log.info("RedisConsumer thread [{}] force shutdown succeeded", Thread.currentThread().getName());
                    }
                } else {
                    log.info("TaskWorker thread [{}] shutdown succeeded", Thread.currentThread().getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }


}
