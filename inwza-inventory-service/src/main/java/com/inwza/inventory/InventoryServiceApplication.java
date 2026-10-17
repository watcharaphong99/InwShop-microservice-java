package com.inwza.inventory;

import com.inwza.inventory.config.JwtProperties;
import com.inwza.inventory.config.KafkaTopicProperties;
import com.inwza.inventory.config.PaginateProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, KafkaTopicProperties.class, PaginateProperties.class})
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
