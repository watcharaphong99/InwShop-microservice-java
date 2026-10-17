package com.inwza.payment.application;

import com.inwza.payment.config.PaymentSagaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentWorkflowRecoveryScheduler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PaymentWorkflowRecoveryScheduler.class);

    private final PaymentSagaService paymentSagaService;
    private final PaymentSagaProperties sagaProperties;

    public PaymentWorkflowRecoveryScheduler(
            PaymentSagaService paymentSagaService, PaymentSagaProperties sagaProperties) {
        this.paymentSagaService = paymentSagaService;
        this.sagaProperties = sagaProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("payment workflow recovery on startup");
        paymentSagaService.recoverStaleOrderWorkflows();
    }

    @Scheduled(fixedDelayString = "${inwza.payment.workflow-recover-interval-ms}")
    public void recoverStaleWorkflows() {
        paymentSagaService.recoverStaleOrderWorkflows();
    }
}
