package com.inwza.auth;

import com.inwza.auth.infrastructure.grpc.PlayerGrpcGateway;
import com.inwza.contracts.player.PlayerProfile;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthLoginIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("inwza_auth")
            .withUsername("inwza")
            .withPassword("inwza");

    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    @MockBean
    PlayerGrpcGateway playerGrpcGateway;

    @Autowired
    MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("grpc.server.port", () -> 0);
    }

    @Test
    void loginReturnsAccessToken() throws Exception {
        when(playerGrpcGateway.credentialSearch(anyString(), anyString()))
                .thenReturn(PlayerProfile.newBuilder()
                        .setId("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa")
                        .setEmail("player001@Iwpshop.com")
                        .setUsername("Player001")
                        .setRoleCode(0)
                        .build());

        mockMvc.perform(post("/auth_v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"player001@Iwpshop.com","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential.access_token").isNotEmpty())
                .andExpect(jsonPath("$.credential.player_id").value("player:aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa"));
    }
}
