package org.example.intershop.service.integration;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class RedisTestContainer {

    @Container
    @ServiceConnection
    public static final RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7.4.2-bookworm"));
}
