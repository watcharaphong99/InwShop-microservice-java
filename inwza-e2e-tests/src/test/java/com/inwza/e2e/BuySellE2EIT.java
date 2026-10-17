package com.inwza.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.inwza.e2e.support.E2ePorts;
import com.inwza.e2e.support.HttpJsonClient;
import com.inwza.e2e.support.MicroserviceProcessLauncher;
import com.redis.testcontainers.RedisContainer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisabledIfSystemProperty(named = "e2e.skip", matches = "true")
class BuySellE2EIT {

    private static final String WOODEN_SWORD = "item:11111111-1111-4111-8111-111111111103";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("inwza_auth")
            .withUsername("inwza")
            .withPassword("inwza")
            .withInitScript("e2e-init-databases.sql");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    private final HttpJsonClient http = new HttpJsonClient();
    private MicroserviceProcessLauncher launcher;
    private String accessToken;
    private String playerId;

    @BeforeAll
    void startStack() throws Exception {
        Path root = Path.of(System.getProperty("user.dir")).getParent();
        launcher = new MicroserviceProcessLauncher(root);

        String dbHost = postgres.getHost();
        String dbPort = String.valueOf(postgres.getFirstMappedPort());
        String kafkaBootstrap = kafka.getBootstrapServers();
        String redisHost = redis.getHost();
        String redisPort = String.valueOf(redis.getMappedPort(6379));

        Map<String, String> db = Map.of(
                "DB_HOST", dbHost,
                "DB_PORT", dbPort,
                "DB_USER", "inwza",
                "DB_PASSWORD", "inwza");

        launcher.startService(
                "inwza-item-service",
                E2ePorts.ITEM_HTTP,
                E2ePorts.ITEM_GRPC,
                merge(db, Map.of("DB_NAME", "inwza_item")));

        launcher.startService(
                "inwza-player-service",
                E2ePorts.PLAYER_HTTP,
                E2ePorts.PLAYER_GRPC,
                merge(
                        db,
                        Map.of(
                                "DB_NAME", "inwza_player",
                                "KAFKA_BOOTSTRAP_SERVERS", kafkaBootstrap,
                                "REDIS_HOST", redisHost,
                                "REDIS_PORT", redisPort)));

        waitHttpOk("http://localhost:" + E2ePorts.ITEM_HTTP + "/item_v1");
        waitHttpOk("http://localhost:" + E2ePorts.PLAYER_HTTP + "/player_v1");

        launcher.startService(
                "inwza-auth-service",
                E2ePorts.AUTH_HTTP,
                E2ePorts.AUTH_GRPC,
                merge(
                        db,
                        Map.of(
                                "DB_NAME", "inwza_auth",
                                "REDIS_HOST", redisHost,
                                "REDIS_PORT", redisPort,
                                "GRPC_PLAYER_ADDRESS", "static://localhost:" + E2ePorts.PLAYER_GRPC)));

        waitHttpOk("http://localhost:" + E2ePorts.AUTH_HTTP + "/auth_v1");

        launcher.startService(
                "inwza-inventory-service",
                E2ePorts.INVENTORY_HTTP,
                E2ePorts.INVENTORY_GRPC,
                merge(
                        db,
                        Map.of(
                                "DB_NAME", "inwza_inventory",
                                "KAFKA_BOOTSTRAP_SERVERS", kafkaBootstrap,
                                "GRPC_ITEM_ADDRESS", "static://localhost:" + E2ePorts.ITEM_GRPC,
                                "PAGINATE_INVENTORY_NEXT_PAGE_BASED_URL",
                                        "http://localhost:" + E2ePorts.INVENTORY_HTTP + "/inventory_v1/inventory")));

        launcher.startService(
                "inwza-payment-service",
                E2ePorts.PAYMENT_HTTP,
                -1,
                merge(
                        db,
                        Map.of(
                                "DB_NAME", "inwza_payment",
                                "KAFKA_BOOTSTRAP_SERVERS", kafkaBootstrap,
                                "GRPC_ITEM_ADDRESS", "static://localhost:" + E2ePorts.ITEM_GRPC)));

        waitHttpOk("http://localhost:" + E2ePorts.INVENTORY_HTTP + "/inventory_v1");
        waitHttpOk("http://localhost:" + E2ePorts.PAYMENT_HTTP + "/payment_v1");

        JsonNode login = http.post(
                "http://localhost:" + E2ePorts.AUTH_HTTP + "/auth_v1/auth/login",
                Map.of("email", "player001@Iwpshop.com", "password", "123456"),
                Optional.empty());
        accessToken = login.path("credential").path("access_token").asText();
        playerId = login.path("credential").path("player_id").asText();
    }

    @AfterAll
    void stopStack() {
        if (launcher != null) {
            launcher.destroyAll();
        }
    }

    @Test
    void buyThenSellWoodenSword() throws Exception {
        double balanceBefore = readBalance();

        http.post(
                "http://localhost:" + E2ePorts.PAYMENT_HTTP + "/payment_v1/payment/buy",
                Map.of("items", List.of(Map.of("item_id", WOODEN_SWORD))),
                Optional.of(accessToken));

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> inventoryCount() >= 1);

        assertEquals(balanceBefore - 100, readBalance(), 0.01);

        http.post(
                "http://localhost:" + E2ePorts.PAYMENT_HTTP + "/payment_v1/payment/sell",
                Map.of("items", List.of(Map.of("item_id", WOODEN_SWORD))),
                Optional.of(accessToken));

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> inventoryCount() == 0);

        assertEquals(balanceBefore - 50, readBalance(), 0.01);
    }

    private double readBalance() throws Exception {
        JsonNode account = http.get(
                "http://localhost:" + E2ePorts.PLAYER_HTTP + "/player_v1/player/saving-account/my-account",
                Optional.of(accessToken));
        return account.path("balance").asDouble();
    }

    private int inventoryCount() throws Exception {
        String pathPlayer = playerId.startsWith("player:") ? playerId.substring("player:".length()) : playerId;
        JsonNode page = http.get(
                "http://localhost:"
                        + E2ePorts.INVENTORY_HTTP
                        + "/inventory_v1/inventory/"
                        + pathPlayer
                        + "?limit=2",
                Optional.of(accessToken));
        JsonNode data = page.get("data");
        return data == null || !data.isArray() ? 0 : data.size();
    }

    private void waitHttpOk(String url) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(3))
                .pollInterval(Duration.ofSeconds(2))
                .ignoreExceptions()
                .until(() -> {
                    int status = http.getStatus(url);
                    return status >= 200 && status < 300;
                });
    }

    private static Map<String, String> merge(Map<String, String> a, Map<String, String> b) {
        var map = new java.util.HashMap<>(a);
        map.putAll(b);
        return map;
    }
}
