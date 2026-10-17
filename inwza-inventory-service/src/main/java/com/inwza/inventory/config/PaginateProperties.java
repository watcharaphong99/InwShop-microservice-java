package com.inwza.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inwza.paginate")
public record PaginateProperties(String inventoryNextPageBaseUrl) {
}
