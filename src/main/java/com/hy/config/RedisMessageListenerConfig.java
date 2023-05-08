package com.hy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.parser.MessageParser;
import com.hy.biz.redis.subscriber.StateChannelSubscriber;
import com.hy.repository.DeviceOnlineStatusRepository;
import com.hy.repository.DeviceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @author shiwentao
 * @package com.hy.config
 * @description
 * @create 2023-05-04 16:06
 **/
@Configuration
public class RedisMessageListenerConfig {
    private final MessageParser messageParser;
    private final HyConfigProperty hyConfigProperty;
    private final DeviceOnlineStatusRepository deviceOnlineStatusRepository;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper mapper;

    public RedisMessageListenerConfig(MessageParser messageParser, HyConfigProperty hyConfigProperty, DeviceOnlineStatusRepository deviceOnlineStatusRepository, DeviceRepository deviceRepository, ObjectMapper mapper) {
        this.messageParser = messageParser;
        this.hyConfigProperty = hyConfigProperty;
        this.deviceOnlineStatusRepository = deviceOnlineStatusRepository;
        this.deviceRepository = deviceRepository;
        this.mapper = mapper;
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, StateChannelSubscriber stateChannelSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(stateChannelSubscriber, new PatternTopic(hyConfigProperty.getDataQueue().getDnmTopicChannel()));
        return container;
    }

    @Bean
    StateChannelSubscriber stateChannelSubscriber() {
        return new StateChannelSubscriber(messageParser, deviceOnlineStatusRepository, deviceRepository, mapper);
    }

}
