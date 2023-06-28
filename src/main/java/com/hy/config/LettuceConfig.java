package com.hy.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

/**
 * ===========================
 * Lettuce 线程池配置          ｜
 * ===========================
 *
 * @author shiwentao
 * @package com.hy.config
 * @create 2023/6/12 12:23
 **/
@Configuration(value = "RedisLettuceClient")
public class LettuceConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.create();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(
                        ClientOptions.builder()
                                .autoReconnect(true)  // 自动重连
                                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)  // 连接断开时不接收Task
                                .socketOptions(SocketOptions.builder().connectTimeout(Duration.ofSeconds(30)).build())  // 连接timeout时限：30秒
                                .build())
                .commandTimeout(Duration.ofSeconds(300)) // 命令timeout时限：5分钟
                .clientResources(clientResources)
                .build();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, 6379);

        return new LettuceConnectionFactory(standaloneConfig, clientConfig);
    }


}
