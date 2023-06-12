package com.hy.biz.dataRead.consumer;

import com.hy.biz.dataPush.task.Task;
import com.hy.biz.dataPush.task.TaskFactory;
import com.hy.biz.dataPush.task.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Redis 消费线程
 */
@Slf4j
public abstract class RedisConsumer {

    private final TaskQueue taskQueue;
    private final TaskFactory taskFactory;
    private final String dataQueue;
    private final String dataBakQueue;
    private final long queueCapacity;
    private final RedisTemplate<String, String> redisTemplate;

    private volatile boolean threadStopped = false;
    private ExecutorService executorService;

    protected RedisConsumer(TaskQueue taskQueue, TaskFactory taskFactory, String dataQueue, String dataBakQueue, int queueCapacity, RedisTemplate<String, String> redisTemplate) {
        this.taskQueue = taskQueue;
        this.taskFactory = taskFactory;
        this.dataQueue = dataQueue;
        this.dataBakQueue = dataBakQueue;
        this.queueCapacity = queueCapacity;
        this.redisTemplate = redisTemplate;
    }

    protected void executeCommand(ThreadFactory threadFactory) {
        executorService = Executors.newSingleThreadExecutor(threadFactory);
        executorService.execute(this::process);
        executorService.shutdown();
    }

    /**
     * 停止线程, 停止从REDIS数据队列获取数据的动作
     */
    protected void stop() {
        threadStopped = true;
        if (executorService != null) {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    //==========================private==========================private==========================private==========================


    private void process() {
        processBackupQueue();
        while (!threadStopped && !Thread.currentThread().isInterrupted())
            processTask();
    }

    private void processBackupQueue() {
        long startPos = 0L;
        long endPos = queueCapacity - 1;

        while (true) {
            List<String> previousTasks = Optional.ofNullable(redisTemplate.opsForList().range(dataBakQueue, startPos, endPos))
                    .orElse(Collections.emptyList());

            if (previousTasks.isEmpty()) break;

            Long count = redisTemplate.opsForList().rightPushAll(dataQueue, previousTasks);
            if (count != null && count > 0) {
                endPos = Math.min(count, endPos);
                Long backupQueueSize = redisTemplate.opsForList().size(dataBakQueue);
                if (backupQueueSize != null)
                    redisTemplate.opsForList().trim(dataBakQueue, endPos + 1, backupQueueSize);
            }
        }
    }

    private void processTask() {
        try {
            String message = redisTemplate.opsForList().rightPopAndLeftPush(dataQueue, dataBakQueue, Duration.ofSeconds(10));
            if (!StringUtils.isBlank(message)) {
                Task task = taskFactory.createTask(message, dataBakQueue);
                if (task != null)
                    taskQueue.putTask(task);
            }
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("LettuceConnectionFactory was destroyed")) stop();
            else throw e;
        }
    }


}
