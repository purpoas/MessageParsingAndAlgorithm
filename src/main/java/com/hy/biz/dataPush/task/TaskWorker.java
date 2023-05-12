package com.hy.biz.dataPush.task;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shiwentao
 * @package com.hy.biz.redis.task
 * @description
 * @create 2023-04-26 14:23
 **/
@Component
public class TaskWorker {
    private final TaskQueue taskQueue;
    private final TaskExecutor taskExecutor;

    private final AtomicBoolean threadStopped = new AtomicBoolean(false);
    private ExecutorService executorService;

    public TaskWorker(TaskQueue taskQueue, @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.taskQueue = taskQueue;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 启动任务队列工作线程
     */
    public void executeTask() {
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        AtomicInteger counter = new AtomicInteger();
        executorService = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = threadFactory.newThread(runnable);
            thread.setName("idds-task-worker-thread" + counter.getAndIncrement());
            return thread;
        });
        executorService.execute(this::command);
    }

    /**
     * 业务处理
     */
    private void command() {

        Task lastTask = null;

        while (!threadStopped.get()) {
            Task task = null;

            try {
                if (lastTask != null) {
                    task = lastTask;
                    TimeUnit.SECONDS.sleep(10L);
                } else {
                    task = taskQueue.takeTask();
                }

                lastTask = null;
                taskExecutor.execute(task);

            } catch (RejectedExecutionException e) {
                lastTask = task;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 程序停止时, 允许正在执行的任务有一段缓冲期，之后再关闭线程.
     */
    @PreDestroy
    public void beanDestroy() {
        threadStopped.set(true);
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

}
