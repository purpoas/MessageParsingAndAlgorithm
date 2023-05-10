package com.hy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.biz.parser.SubscribedMessageParser;
import com.hy.biz.redis.subscriber.StateChannelSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final SubscribedMessageParser subscribedMessageParser;
    private final RedisTemplate<String, String> redisTemplate;
    private final HyConfigProperty hyConfigProperty;
    private final ObjectMapper mapper;

    public RedisMessageListenerConfig(HyConfigProperty hyConfigProperty, SubscribedMessageParser subscribedMessageParser, RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        this.subscribedMessageParser = subscribedMessageParser;
        this.hyConfigProperty = hyConfigProperty;
        this.redisTemplate = redisTemplate;
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
        return new StateChannelSubscriber(subscribedMessageParser, redisTemplate, mapper);
    }

}
