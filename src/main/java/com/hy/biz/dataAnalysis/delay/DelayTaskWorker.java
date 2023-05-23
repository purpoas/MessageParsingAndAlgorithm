package com.hy.biz.dataAnalysis.delay;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hy.biz.cache.service.AlgorithmCacheManager;
import com.hy.biz.dataAnalysis.DataAnalysisService;
import com.hy.biz.dataAnalysis.dto.AlgorithmIdentify;
import com.hy.biz.dataAnalysis.dto.AlgorithmTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Component
public class DelayTaskWorker {

    private final Logger log = LoggerFactory.getLogger(DelayTaskWorker.class);

    @Autowired
    @Qualifier("algorithmTaskExecutor")
    private TaskExecutor taskExecutor;

    @Autowired
    private DelayTaskQueue delayTaskQueue;

    @Autowired
    private DataAnalysisService dataAnalysisService;

    @Autowired
    private AlgorithmCacheManager algorithmCacheManager;

    // 线程池
    private ExecutorService executorService = null;
    // 线程停止标志
    private volatile boolean stopThread = false;


    /**
     * 启动延迟任务队列工作线程
     */
    public void start() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("delay-task-worker").build();
        executorService = Executors.newSingleThreadExecutor(threadFactory);
        executorService.execute(() -> process());
        executorService.shutdown();
    }

    /**
     * 业务处理
     */
    private void process() {

        while (true) {
            try {

                if (stopThread) {
                    log.debug("DelayTaskWorker has [{}] tasks to deal with", delayTaskQueue.size());
                    break;
                }

                AlgorithmIdentify identify = delayTaskQueue.take();
                log.info("[DELAY] AlgorithmIdentify ： {}", identify.toString());

                // 从算法任务缓存中取出算法任务
                AlgorithmTask task = algorithmCacheManager.get(identify);

                if (task == null) {
                    log.error("[DELAY] cache not exist , AlgorithmIdentify ： {}", identify.toString());
                    continue;
                }

                // 生成Runnable任务
                DelayAlgorithmTask delayAlgorithmTask = new DelayAlgorithmTask(identify, task, dataAnalysisService);
                // 异步线程执行任务
                taskExecutor.execute(delayAlgorithmTask);
            } catch (Exception e) {
                log.error("[DELAY] Error: {}", e.getMessage());
            }
        }

    }

    /**
     * 程序停止时, 设置业务处理停止标志, 并关闭线程.
     */
    @PreDestroy
    public void beanDestroy() {
        log.info("[DELAY] thread is going to stop");
        this.stopThread = true;
        if (executorService != null) {
            try {
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    log.info("[DELAY] thread shutdown failed");
                    executorService.shutdownNow();
                } else {
                    log.info("[DELAY] thread shutdown succeeded");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[DELAY] thread shutdown failed , Error : {}", e.getMessage());
            }
        }
    }


}
