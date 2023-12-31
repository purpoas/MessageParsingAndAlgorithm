package com.hy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author shiwentao
 * @package com.hy.config
 * @description
 * @create 2023-04-25 10:56
 **/

@Configuration
@ConfigurationProperties(prefix = "hy")
@Data
public class HyConfigProperty {

    private final Async async = new Async();                                // 异步线程池配置

    private final DataReadConfig dataRead = new DataReadConfig();           // 数据读取配置

    private final DataQueue dataQueue = new DataQueue();                    //Redis配置

    private final Constant constant = new Constant();                       // 基础配置

    private final AlgorithmConfig algorithm = new AlgorithmConfig();        // 算法配置

    /**
     * 异步任务线程池配置
     */
    @Data
    public static class Async {

        private int corePoolSize = 2;

        private int maxPoolSize = 50;

        private int queueCapacity = 5000;

    }

    /**
     * Data read mode , support REDIS or ROCKET or MQTT or MYSQL
     */
    @Data
    public static class DataReadConfig {

        private String mode = "REDISMQ";

    }


    @Data
    public static class DataQueue {
        /**
         * redis消息队列
         */
        private String dnmData;
        private Integer queueCapacity;
        private String dnmDataBak;
        private String dnmDataUnknown;

        /**
         * redis订阅通知
         */
        private String dnmTopicChannelSb;
        private String dnmTopicChannelPb;
    }

    @Data
    public static class Constant {
        private String dnmLatestDeviceStatus;
        private String supplierCode;                          //供应商编号
        private Integer travelThreshold;                      //行波阈值
        private Integer frequencyThreshold;                   //工频阈值
        private Integer travelMaxThreshold;                   //行波最大阈值
        private Integer frequencyMaxThreshold;                //工频最大阈值
        private Long travelSampleRate;                        //行波采样率
        private Long deviceSampleRate;                     //工频采样率
        private Long faultFrequencyCurrentThreshold;          //工频电流故障触发阈值
        private Integer cycleWaveLength;                      //周波长度
        private Double speed;                                 //波形传播速度 默认单位 290 m/us
        private String accidentFilePath;                      //存储事故ID文件位置
        private String faultFilePath;                         //存储故障波形推送失败的文件夹
        private String batteryFilePath;                       //电池电量对应关联文件位置
    }

    /**
     * 算法相关配置
     */
    @Data
    public static class AlgorithmConfig {

        private int delayExecuteTime = 10; // 算法任务延迟执行时间 单位秒

        private int Iset = 10;       // 过流定值   每条线路配置不同
        private int I0set = 10;      // 零序定值   每条线路配置不同

        private int IMAX = 600; //短路保护整定值
        private int I0AM = 2;   //零序电流阈值
        private double IBPH_MAX = 0.9;  //三相不平衡度
        private int IMIN = 2;   //最小工作电流
        private int UMIN = 600; //最小工电压
        private double IH2 = 0.15;  //二次谐波含量


    }

}
