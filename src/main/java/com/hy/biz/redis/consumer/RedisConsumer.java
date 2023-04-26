package com.hy.biz.redis.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.parser.MessageParser;
import com.hy.biz.parser.entity.dto.MessageDTO;
import com.hy.biz.redis.task.MessageParsingTask;
import com.hy.biz.redis.task.Task;
import com.hy.biz.redis.task.TaskQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class RedisConsumer {
    private final MessageParser messageParser;
    private final TaskQueue taskQueue;
    private final String workingQueue;
    private final String backupQueue;
    private final RedisTemplate<String, String> redisTemplate;
    private volatile boolean threadStopped = false;
    private ExecutorService executorService;
    private static final long REDIS_LS_MAX_SIZE = 5000L;

    protected RedisConsumer(MessageParser messageParser, TaskQueue taskQueue, String workingQueue, String backupQueue,
                            RedisTemplate<String, String> redisTemplate) {
        this.messageParser = messageParser;
        this.taskQueue = taskQueue;
        this.workingQueue = workingQueue;
        this.backupQueue = backupQueue;
        this.redisTemplate = redisTemplate;
    }

    private void process() {
        long startPosition = 0L;
        long endPosition = REDIS_LS_MAX_SIZE - 1;

        processBackupQueue(startPosition, endPosition);

        while (!threadStopped) {
            processWorkingQueue();
        }
    }

    private void processBackupQueue(long startPosition, long endPosition) {
        while (true) {
            try {
                List<String> previousTasks = redisTemplate.opsForList().range(backupQueue, startPosition, endPosition);

                if (previousTasks == null || previousTasks.isEmpty()) {
                    break;
                }

                Optional<Long> optionalCount = Optional.ofNullable(redisTemplate.opsForList().rightPushAll(workingQueue, previousTasks));

                if (optionalCount.isPresent()) {
                    long count = optionalCount.get();
                    if (count > 0) {
                        if (count < endPosition) {
                            endPosition = count;
                        }

                        Optional<Long> optionalBackupQueueSize = Optional.ofNullable(redisTemplate.opsForList().size(backupQueue));

                        if (optionalBackupQueueSize.isPresent()) {
                            long backupQueueSize = optionalBackupQueueSize.get();
                            redisTemplate.opsForList().trim(backupQueue, endPosition - 1, backupQueueSize);
                        }

                    }
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void processWorkingQueue() {
        try {
            String message = redisTemplate.opsForList().rightPopAndLeftPush(workingQueue, backupQueue, Duration.ofSeconds(10));

            if (StringUtils.isNotBlank(message)) {
                ObjectMapper objectMapper = new ObjectMapper();
                MessageDTO messageDTO = objectMapper.readValue(message, MessageDTO.class);
                Task task = new MessageParsingTask(messageParser, messageDTO);

                taskQueue.putTask(task);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void start(ThreadFactory threadFactory) {
        executorService = Executors.newSingleThreadExecutor(threadFactory);
        executorService.execute(this::process);
        executorService.shutdown();
    }

    protected void stop() {
        this.threadStopped = true;
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
