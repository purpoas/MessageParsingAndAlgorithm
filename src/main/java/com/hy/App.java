package com.hy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class App {
    private final static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        ConfigurableEnvironment env = app.run(args).getEnvironment();
        log.info("\n--+---+---+---+---+---+---+---+---+---+---+---+---+---+--\n" +
                        "Application {} is running! Access URLs:\n" +
                        "\tLocal:\thttp://localhost:{}\n" +
                        "Server Info:\n" +
                        "\tDatabase:\t{}\n" +
                        "\tRedis:\t{}:{}\n" +
                        "--+---+---+---+---+---+---+---+---+---+---+---+---+---+--",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                env.getProperty("spring.datasource.url"),
                env.getProperty("spring.redis.host"),
                env.getProperty("spring.redis.port"));
    }
}
