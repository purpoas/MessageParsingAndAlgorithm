package com.hy.biz.dataPush.task;

/**
 * =============
 * 任务工厂     ｜
 * =============
 *
 * @author shiwentao
 * @package com.hy.biz.dataPush.task
 * @create 2023/6/26 16:48
 **/
public interface TaskFactory {

    /**
     * 创建任务
     *
     * @param message 报文内容
     * @param backupQueue backupQueue
     * @return 任务
     */
    Task createTask(String message, String backupQueue);


}
