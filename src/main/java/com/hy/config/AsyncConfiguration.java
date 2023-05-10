package com.hy.config;

import com.hy.async.ExceptionHandlingAsyncTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer {

    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    @Autowired
    private HyConfigProperty hyConfigProperty;

    @Override
    @Bean(name = "taskExecutor")
    public TaskExecutor getAsyncExecutor() {
        log.info("Creating Async Task Executor\n" +
                "\tCorePoolSize:\t{}\n" +
                "\tMaxPoolSize:\t{}\n" +
                "\tQueueCapacity:\t{} ",
                hyConfigProperty.getAsync().getCorePoolSize(),
                hyConfigProperty.getAsync().getMaxPoolSize(),
                hyConfigProperty.getAsync().getQueueCapacity());
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(hyConfigProperty.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(hyConfigProperty.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(hyConfigProperty.getAsync().getQueueCapacity());
        executor.setThreadNamePrefix("idds-exec-");
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
