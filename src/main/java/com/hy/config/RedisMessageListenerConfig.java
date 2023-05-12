package com.hy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.dataResolver.handler.MessageHandler;
import com.hy.biz.dataPush.subscriber.StateChannelSubscriber;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author shiwentao
 * @package com.hy.config
 * @description
 * @create 2023-05-04 16:06
 **/
@Configuration
public class RedisMessageListenerConfig {
    private final ApplicationContext context;
    private final HyConfigProperty hyConfigProperty;
    private final ObjectMapper mapper;

    public RedisMessageListenerConfig(ApplicationContext context, HyConfigProperty hyConfigProperty, ObjectMapper mapper) {
        this.context = context;
        this.hyConfigProperty = hyConfigProperty;
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
        Map<String, MessageHandler> messageHandlers = context.getBeansOfType(MessageHandler.class);
        return new StateChannelSubscriber(new ArrayList<>(messageHandlers.values()), mapper);
    }

}
