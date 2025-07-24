package org.example.intershop.service.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ImportTestcontainers({PostgresTestContainer.class, RedisTestContainer.class})
@SpringBootTest
@ActiveProfiles("test")
@Import(AbstractIntegration.MockSecurityConfig.class)
public class AbstractIntegration {

    @TestConfiguration
    public static class MockSecurityConfig {

        @Bean
        @Primary
        public ReactiveClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration registration = ClientRegistration.withRegistrationId("yandex-store")
                    .clientId("yandex-store")
                    .clientSecret("mock-secret")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .tokenUri("http://localhost:8888/realms/master/protocol/openid-connect/token")
                    .jwkSetUri("http://localhost:8888/realms/master/protocol/openid-connect/certs")
                    .issuerUri("http://localhost:8888/realms/master")
                    .build();

            return new InMemoryReactiveClientRegistrationRepository(registration);
        }

        @Bean
        @Primary
        public ReactiveOAuth2AuthorizedClientService authorizedClientService() {
            return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository());
        }
    }
}
