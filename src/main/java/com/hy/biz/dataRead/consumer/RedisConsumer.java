package com.hy.biz.dataRead.consumer;

import com.hy.biz.dataPush.task.Task;
import com.hy.biz.dataPush.task.TaskFactory;
import com.hy.biz.dataPush.task.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
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
    }

    protected void process() {

        log.info("RedisConsumer thread [{}] is going to start.", Thread.currentThread().getName());

        long startPos = 0L;
        long endPos = queueCapacity - 1;
        List<String> previousTasks;

        while (true) {
            try {
                previousTasks = redisTemplate.opsForList().range(dataBakQueue, startPos, endPos);

                if (previousTasks.size() == 0) break;
                else {
                    Long count = redisTemplate.opsForList().rightPushAll(dataQueue, previousTasks);
                    if (count > 0) {
                        if (count < endPos) {
                            endPos = count;
                        }

                        long backupQueueSize = redisTemplate.opsForList().size(dataBakQueue);

                        redisTemplate.opsForList().trim(dataBakQueue, endPos + 1, backupQueueSize);
                    }
                    log.info("RedisConsumer thread [{}] return {} tasks to queue {}", Thread.currentThread().getName(), count, dataQueue);
                }
            } catch (OutOfMemoryError e) {
                log.error("内存错误: ", e);
                break;
            }
        }

        while (true) {
            // 线程停止标志置位
            if (threadStopped) {
                log.info("Redis consumer will stop to fetch data");
                break;
            }

            try {
                String message = redisTemplate.opsForList().rightPopAndLeftPush(dataQueue, dataBakQueue, Duration.ofSeconds(5));
                if (!StringUtils.isBlank(message)) {
                    Task task = taskFactory.createTask(message, dataBakQueue);
                    if (task != null) {
                        taskQueue.putTask(task);
                        log.info("Task [{}] added to the queue", task);
                    }
                }
            } catch (Exception e) {
                log.error("从 Redis 阻塞队列中读取报文数据失败", e);
            }
        }
    }

    /**
     * 停止线程, 停止从REDIS数据队列获取数据的动作
     */
    protected void stop() {
        log.info("RedisConsumer thread [{}] is going to stop", Thread.currentThread().getName());
        threadStopped = true;
        if (executorService != null) {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    log.info("RedisConsumer thread [{}] shutdown failed", Thread.currentThread().getName());
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                        log.info("RedisConsumer thread [{}] force shutdown failed", Thread.currentThread().getName());
                    } else {
                        log.info("RedisConsumer thread [{}] force shutdown succeeded", Thread.currentThread().getName());
                    }
                } else {
                    log.info("RedisConsumer thread [{}] shutdown succeeded", Thread.currentThread().getName());
                }
                if (!executorService.isTerminated()) {
                    log.error("RedisConsumer thread [{}] didn't shut down correctly", Thread.currentThread().getName());
                } else {
                    log.info("RedisConsumer thread [{}] has stopped successfully", Thread.currentThread().getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("RedisConsumer thread [{}] was interrupted during shutdown", Thread.currentThread().getName(), e);
            }
        }
    }


}
