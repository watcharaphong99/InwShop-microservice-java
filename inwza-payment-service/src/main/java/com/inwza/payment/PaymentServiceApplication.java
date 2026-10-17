package com.inwza.payment;

import com.inwza.payment.config.JwtProperties;
import com.inwza.payment.config.KafkaTopicProperties;
import com.inwza.payment.config.PaymentSagaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, KafkaTopicProperties.class, PaymentSagaProperties.class})
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
