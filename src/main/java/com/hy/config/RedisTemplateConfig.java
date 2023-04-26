package com.hy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created by Administrator on 2020/8/14.
 *
 * @author WeiQuanfu
 */
@Configuration
public class RedisTemplateConfig {
    @Bean
    @Primary
    public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, String> rt = new RedisTemplate<>();
        rt.setConnectionFactory(redisConnectionFactory);
        rt.setKeySerializer(new StringRedisSerializer());
        rt.setValueSerializer(new StringRedisSerializer());
        rt.setHashValueSerializer(new StringRedisSerializer());
        rt.setHashKeySerializer(new StringRedisSerializer());
        return rt;
    }

    @Bean
    public RedisTemplate<String,Long> longRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Long> rt = new RedisTemplate<>();
        rt.setConnectionFactory(redisConnectionFactory);
        rt.setKeySerializer(new StringRedisSerializer());
        rt.setValueSerializer(new RedisSerializer<Long>() {
            @Override
            public byte[] serialize(Long aLong) throws SerializationException {
                return aLong.toString().getBytes();
            }

            @Override
            public Long deserialize(byte[] bytes) throws SerializationException {
                return Long.parseLong(new String(bytes));
            }
        });
        return rt;
    }
}
