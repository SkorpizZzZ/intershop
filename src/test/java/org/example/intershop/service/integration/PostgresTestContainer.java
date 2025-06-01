package org.example.intershop.service.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class PostgresTestContainer {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgreSqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
}
