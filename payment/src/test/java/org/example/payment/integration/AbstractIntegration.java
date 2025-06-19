package org.example.payment.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ImportTestcontainers(PostgresTestContainer.class)
@SpringBootTest
@ActiveProfiles("test")
public class AbstractIntegration {
}
