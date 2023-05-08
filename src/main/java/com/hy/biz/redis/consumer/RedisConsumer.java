package com.hy.biz.redis.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.parser.entity.dto.MessageDTO;
import com.hy.biz.redis.task.Task;
import com.hy.biz.redis.task.TaskFactory;
import com.hy.biz.redis.task.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class RedisConsumer {
    private final TaskQueue taskQueue;
    private final TaskFactory taskFactory;
    private final String dataQueue;
    private final String dataBakQueue;
    private final int queueCapacity;
    private final RedisTemplate<String, String> redisTemplate;

    private final AtomicBoolean threadStopped = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
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
        AtomicInteger counter = new AtomicInteger();
        executorService = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = threadFactory.newThread(runnable);
            thread.setName("idds-message-consumer-thread-" + counter.getAndIncrement());
            return thread;
        });
        executorService.execute(this::command);
    }

    private void command() {
        int startPosition = 0;
        int endPosition = queueCapacity - 1;

        processBackupQueue(startPosition, endPosition);

        while (!threadStopped.get()) {
            processWorkingQueue();
        }
    }

    private void processBackupQueue(long startPosition, long endPosition) {
        while (true) {
            try {
                Optional<List<String>> unresolvedTasks = Optional.ofNullable(redisTemplate.opsForList().range(dataBakQueue, startPosition, endPosition));

                if (unresolvedTasks.isPresent()) {
                    if (unresolvedTasks.get().isEmpty())
                        break;
                    else {
                        Optional<Long> count = Optional.ofNullable(redisTemplate.opsForList().rightPushAll(dataQueue, unresolvedTasks.get()));

                        if (count.isPresent()) {
                            if (count.get() > 0) {
                                Long backupQueueSize = redisTemplate.opsForList().size(dataBakQueue);
                                assert backupQueueSize != null;
                                redisTemplate.opsForList().trim(dataBakQueue, count.get(), backupQueueSize);
                            }
                        }
                    }
                } else {
                    log.error("unresolvedTasks 为 null");
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void processWorkingQueue() {
        String message = redisTemplate.opsForList().rightPopAndLeftPush(dataQueue, dataBakQueue, Duration.ofSeconds(10));

        if (StringUtils.isNotBlank(message)) {
            MessageDTO messageDTO;
            try {
                messageDTO = objectMapper.readValue(message, MessageDTO.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            Task task = taskFactory.createTask(message, messageDTO, dataBakQueue);

            taskQueue.putTask(task);
        }

    }

    protected void stop() {
        this.threadStopped.set(true);
        if (executorService != null) {
            executorService.shutdownNow();
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
