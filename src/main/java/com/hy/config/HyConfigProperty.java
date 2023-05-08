package com.hy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @package com.hy.config
 * @description
 * @author shiwentao
 * @create 2023-04-25 10:56
 **/

@Configuration
@ConfigurationProperties(prefix = "hy")
@Data
public class HyConfigProperty {

    private final DataQueue dataQueue = new DataQueue();                    //Redis配置

    private final Constant constant = new Constant();                       // 基础配置

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
        private String dnmTopicChannel;
    }

    @Data
    public static class Constant {
        private String supplierCode;                 //供应商编号
        private Integer travelThreshold;             //行波阈值
        private Integer frequencyThreshold;          //工频阈值
        private Integer travelMaxThreshold;          //行波最大阈值
        private Integer frequencyMaxThreshold;       //工频最大阈值
        private Long travelSampleRate;               //行波采样率
        private Long frequencySampleRate;            //工频采样率
        private Double speed;                        //波形传播速度 默认单位 m/ns
        private String accidentFilePath;             //存储事故ID文件位置
        private String faultFilePath;                //存储故障波形推送失败的文件夹
        private String batteryFilePath;              //电池电量对应关联文件位置
    }

}
