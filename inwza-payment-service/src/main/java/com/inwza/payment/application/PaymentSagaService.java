package com.inwza.payment.application;

import com.inwza.contracts.item.FindItemsInIdsRes;
import com.inwza.contracts.item.Item;
import com.inwza.payment.application.messaging.PaymentKafkaPublisher;
import com.inwza.payment.application.messaging.PaymentReplyRegistry;
import com.inwza.payment.application.messaging.PaymentTransferMessage;
import com.inwza.payment.application.messaging.PlayerTransactionMessage;
import com.inwza.payment.application.messaging.RollbackInventoryMessage;
import com.inwza.payment.application.messaging.RollbackTransactionMessage;
import com.inwza.payment.application.messaging.UpdateInventoryMessage;
import com.inwza.payment.config.PaymentSagaProperties;
import com.inwza.payment.infrastructure.grpc.ItemGrpcGateway;
import com.inwza.payment.infrastructure.persistence.OrderWorkflowEntity;
import com.inwza.payment.infrastructure.persistence.OrderWorkflowJpaRepository;
import com.inwza.payment.infrastructure.persistence.WorkflowStepJson;
import com.inwza.payment.api.dto.PaymentDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

@Service
public class PaymentSagaService {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaService.class);

    static final String WORKFLOW_RUNNING = "running";
    static final String WORKFLOW_COMPLETED = "completed";
    static final String WORKFLOW_FAILED = "failed";
    static final String STEP_SENT = "sent";
    static final String STEP_DONE = "done";

    static final String STEP_CAPTURE_PAYMENT = "capture_payment";
    static final String STEP_FULFILL_LINE_ITEM = "fulfill_line_item";
    static final String STEP_ADD_MONEY = "add_money";
    static final String STEP_REMOVE_ITEM = "remove_item";
    static final String STEP_CAPTURE_PAYMENT_LEGACY = "dock_money";
    static final String STEP_FULFILL_LINE_ITEM_LEGACY = "add_item";

    private final ItemGrpcGateway itemGrpcGateway;
    private final PaymentKafkaPublisher kafkaPublisher;
    private final PaymentReplyRegistry replyRegistry;
    private final OrderWorkflowJpaRepository workflowRepository;
    private final PaymentSagaProperties sagaProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public PaymentSagaService(
            ItemGrpcGateway itemGrpcGateway,
            PaymentKafkaPublisher kafkaPublisher,
            PaymentReplyRegistry replyRegistry,
            OrderWorkflowJpaRepository workflowRepository,
            PaymentSagaProperties sagaProperties) {
        this.itemGrpcGateway = itemGrpcGateway;
        this.kafkaPublisher = kafkaPublisher;
        this.replyRegistry = replyRegistry;
        this.workflowRepository = workflowRepository;
        this.sagaProperties = sagaProperties;
    }

    public void acceptPaymentReply(PaymentTransferMessage message) {
        replyRegistry.accept(message);
    }

    public List<PaymentTransferMessage> executePurchase(String playerId, PaymentDtos.ItemServiceRequest request) {
        List<PaymentDtos.ItemLine> lines = prepareItems(request);
        OrderWorkflowEntity workflow = newWorkflow(playerId, "buy");
        saveWorkflow(workflow);

        for (PaymentDtos.ItemLine line : lines) {
            PaymentStep step = new PaymentStep(newEventId(), line.itemId(), line.price(), null, null);
            trackStep(workflow, step, STEP_CAPTURE_PAYMENT, STEP_SENT);
            PaymentTransferMessage res = capturePayment(playerId, step);
            if (res == null || res.error() != null && !res.error().isBlank()) {
                logStep("capture payment", step.eventId(), res, null);
                throw abortOrderWorkflow(workflow);
            }
            finishStep(workflow, res.transactionId(), null);
        }

        List<PaymentTransferMessage> out = new ArrayList<>();
        for (PaymentStep paid : doneSteps(workflow, STEP_CAPTURE_PAYMENT)) {
            PaymentStep step =
                    new PaymentStep(newEventId(), paid.itemId(), paid.amount(), paid.transactionId(), null);
            trackStep(workflow, step, STEP_FULFILL_LINE_ITEM, STEP_SENT);
            PaymentTransferMessage res = fulfillLineItem(playerId, step);
            if (res == null || res.error() != null && !res.error().isBlank()) {
                logStep("fulfill line item", step.eventId(), res, null);
                throw abortOrderWorkflow(workflow);
            }
            finishStep(workflow, paid.transactionId(), res.inventoryId());
            out.add(new PaymentTransferMessage(
                    step.eventId(),
                    res.inventoryId(),
                    paid.transactionId(),
                    playerId,
                    paid.itemId(),
                    paid.amount(),
                    null));
        }

        workflow.setStatus(WORKFLOW_COMPLETED);
        saveWorkflow(workflow);
        return out;
    }

    public List<PaymentTransferMessage> sellItem(String playerId, PaymentDtos.ItemServiceRequest request) {
        List<PaymentDtos.ItemLine> lines = prepareItems(request);
        OrderWorkflowEntity workflow = newWorkflow(playerId, "sell");
        saveWorkflow(workflow);

        for (PaymentDtos.ItemLine line : lines) {
            PaymentStep step = new PaymentStep(newEventId(), line.itemId(), line.price(), null, null);
            trackStep(workflow, step, STEP_REMOVE_ITEM, STEP_SENT);
            PaymentTransferMessage res = removeItem(playerId, step);
            if (res == null || res.error() != null && !res.error().isBlank()) {
                logStep("sell remove item", step.eventId(), res, null);
                throw abortOrderWorkflow(workflow);
            }
            finishStep(workflow, null, null);
        }

        List<PaymentTransferMessage> out = new ArrayList<>();
        for (PaymentStep item : doneSteps(workflow, STEP_REMOVE_ITEM)) {
            double payout = sellPayout(item.amount());
            if (payout <= 0) {
                log.error("sell payout is zero item_id={}", item.itemId());
                throw abortOrderWorkflow(workflow);
            }
            PaymentStep step = new PaymentStep(newEventId(), item.itemId(), payout, null, null);
            trackStep(workflow, step, STEP_ADD_MONEY, STEP_SENT);
            PaymentTransferMessage res = addMoney(playerId, step);
            if (res == null || res.error() != null && !res.error().isBlank()) {
                logStep("sell add money", step.eventId(), res, null);
                throw abortOrderWorkflow(workflow);
            }
            finishStep(workflow, res.transactionId(), null);
            out.add(new PaymentTransferMessage(
                    step.eventId(),
                    null,
                    res.transactionId(),
                    playerId,
                    item.itemId(),
                    item.amount(),
                    null));
        }

        workflow.setStatus(WORKFLOW_COMPLETED);
        saveWorkflow(workflow);
        return out;
    }

    @Transactional(readOnly = true)
    public void recoverStaleOrderWorkflows() {
        Instant olderThan = Instant.now().minusSeconds(sagaProperties.workflowStaleSeconds());
        List<OrderWorkflowEntity> stale = workflowRepository.findStaleRunning(olderThan);
        for (OrderWorkflowEntity workflow : stale) {
            log.info("recover stale order workflow {} action={}", workflow.getId(), workflow.getAction());
            compensate(workflow);
            workflow.setStatus(WORKFLOW_FAILED);
            saveWorkflow(workflow);
        }
    }

    private List<PaymentDtos.ItemLine> prepareItems(PaymentDtos.ItemServiceRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new PaymentDomainException("error: items is empty");
        }
        List<PaymentDtos.ItemLine> lines = new ArrayList<>(request.items());
        resolvePricingFromCatalog(lines);
        for (PaymentDtos.ItemLine line : lines) {
            if (line.price() <= 0) {
                throw new PaymentDomainException("error: item price is invalid");
            }
        }
        return lines;
    }

    private void resolvePricingFromCatalog(List<PaymentDtos.ItemLine> lines) {
        Set<String> ids = new HashSet<>();
        for (PaymentDtos.ItemLine line : lines) {
            ids.add(line.itemId());
        }
        FindItemsInIdsRes catalog = itemGrpcGateway.findItemsInIds(new ArrayList<>(ids));
        Map<String, Item> byId = new HashMap<>();
        for (Item item : catalog.getItemsList()) {
            byId.put(item.getId(), item);
        }
        for (int i = 0; i < lines.size(); i++) {
            PaymentDtos.ItemLine line = lines.get(i);
            Item found = byId.get(line.itemId());
            if (found == null) {
                throw new PaymentDomainException("error: items not found");
            }
            lines.set(i, new PaymentDtos.ItemLine(line.itemId(), found.getPrice()));
        }
    }

    private PaymentTransferMessage capturePayment(String playerId, PaymentStep step) {
        BlockingQueue<PaymentTransferMessage> queue = replyRegistry.register(step.eventId());
        try {
            kafkaPublisher.capturePayment(new PlayerTransactionMessage(playerId, -step.amount(), step.eventId()));
            return replyRegistry.waitReply(step.eventId(), queue, sagaProperties.stepTimeoutMs());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PaymentDomainException("error: payment step cancelled");
        } catch (PaymentReplyRegistry.PaymentReplyException ex) {
            throw new PaymentDomainException(ex.getMessage());
        } finally {
            replyRegistry.unregister(step.eventId());
        }
    }

    private PaymentTransferMessage addMoney(String playerId, PaymentStep step) {
        BlockingQueue<PaymentTransferMessage> queue = replyRegistry.register(step.eventId());
        try {
            kafkaPublisher.addPlayerMoney(new PlayerTransactionMessage(playerId, step.amount(), step.eventId()));
            return replyRegistry.waitReply(step.eventId(), queue, sagaProperties.stepTimeoutMs());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PaymentDomainException("error: payment step cancelled");
        } catch (PaymentReplyRegistry.PaymentReplyException ex) {
            throw new PaymentDomainException(ex.getMessage());
        } finally {
            replyRegistry.unregister(step.eventId());
        }
    }

    private PaymentTransferMessage fulfillLineItem(String playerId, PaymentStep step) {
        BlockingQueue<PaymentTransferMessage> queue = replyRegistry.register(step.eventId());
        try {
            kafkaPublisher.addPlayerItem(
                    new UpdateInventoryMessage(playerId, step.itemId(), step.eventId()));
            return replyRegistry.waitReply(step.eventId(), queue, sagaProperties.stepTimeoutMs());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PaymentDomainException("error: payment step cancelled");
        } catch (PaymentReplyRegistry.PaymentReplyException ex) {
            throw new PaymentDomainException(ex.getMessage());
        } finally {
            replyRegistry.unregister(step.eventId());
        }
    }

    private PaymentTransferMessage removeItem(String playerId, PaymentStep step) {
        BlockingQueue<PaymentTransferMessage> queue = replyRegistry.register(step.eventId());
        try {
            kafkaPublisher.removePlayerItem(
                    new UpdateInventoryMessage(playerId, step.itemId(), step.eventId()));
            return replyRegistry.waitReply(step.eventId(), queue, sagaProperties.stepTimeoutMs());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PaymentDomainException("error: payment step cancelled");
        } catch (PaymentReplyRegistry.PaymentReplyException ex) {
            throw new PaymentDomainException(ex.getMessage());
        } finally {
            replyRegistry.unregister(step.eventId());
        }
    }

    private PaymentDomainException abortOrderWorkflow(OrderWorkflowEntity workflow) {
        compensate(workflow);
        workflow.setStatus(WORKFLOW_FAILED);
        saveWorkflow(workflow);
        if ("sell".equals(workflow.getAction())) {
            return new PaymentDomainException("error: sell item failed");
        }
        return new PaymentDomainException("error: buy item failed");
    }

    private void compensate(OrderWorkflowEntity workflow) {
        List<PaymentStep> money = new ArrayList<>();
        List<PaymentStep> added = new ArrayList<>();
        List<PaymentStep> removed = new ArrayList<>();
        for (WorkflowStepJson step : workflow.getSteps()) {
            if (!STEP_DONE.equals(step.getStatus())) {
                continue;
            }
            PaymentStep recorded = new PaymentStep(
                    step.getEventId(),
                    step.getItemId(),
                    step.getAmount(),
                    step.getTransactionId(),
                    step.getInventoryId());
            switch (step.getKind()) {
                case STEP_CAPTURE_PAYMENT, STEP_CAPTURE_PAYMENT_LEGACY, STEP_ADD_MONEY -> money.add(recorded);
                case STEP_FULFILL_LINE_ITEM, STEP_FULFILL_LINE_ITEM_LEGACY -> added.add(recorded);
                case STEP_REMOVE_ITEM -> removed.add(recorded);
                default -> {
                }
            }
        }
        rollbackAddedItems(added);
        rollbackRemovedItems(workflow.getPlayerId(), removed);
        rollbackMoney(money);
    }

    private void rollbackMoney(List<PaymentStep> steps) {
        for (PaymentStep step : steps) {
            try {
                kafkaPublisher.rollbackTransaction(
                        new RollbackTransactionMessage(step.transactionId(), step.eventId()));
            } catch (Exception ex) {
                log.error("rollback money event_id={}: {}", step.eventId(), ex.getMessage());
            }
        }
    }

    private void rollbackAddedItems(List<PaymentStep> steps) {
        for (PaymentStep step : steps) {
            try {
                kafkaPublisher.rollbackAddPlayerItem(
                        new RollbackInventoryMessage(step.inventoryId(), step.eventId(), null, null));
            } catch (Exception ex) {
                log.error("rollback add item event_id={}: {}", step.eventId(), ex.getMessage());
            }
        }
    }

    private void rollbackRemovedItems(String playerId, List<PaymentStep> steps) {
        for (PaymentStep step : steps) {
            try {
                kafkaPublisher.rollbackRemovePlayerItem(new RollbackInventoryMessage(
                        null, step.eventId(), playerId, step.itemId()));
            } catch (Exception ex) {
                log.error("rollback remove item event_id={}: {}", step.eventId(), ex.getMessage());
            }
        }
    }

    private OrderWorkflowEntity newWorkflow(String playerId, String action) {
        OrderWorkflowEntity workflow = new OrderWorkflowEntity();
        workflow.setId(newEventId());
        workflow.setPlayerId(playerId);
        workflow.setAction(action);
        workflow.setStatus(WORKFLOW_RUNNING);
        workflow.setUpdatedAt(Instant.now());
        workflow.setSteps(new ArrayList<>());
        return workflow;
    }

    private void trackStep(OrderWorkflowEntity workflow, PaymentStep step, String kind, String status) {
        workflow.getSteps().add(new WorkflowStepJson(
                step.eventId(),
                kind,
                step.itemId(),
                step.amount(),
                step.transactionId(),
                step.inventoryId(),
                status));
        saveWorkflow(workflow);
    }

    private void finishStep(OrderWorkflowEntity workflow, String transactionId, String inventoryId) {
        WorkflowStepJson last = workflow.getSteps().get(workflow.getSteps().size() - 1);
        last.setStatus(STEP_DONE);
        if (transactionId != null && !transactionId.isBlank()) {
            last.setTransactionId(transactionId);
        }
        if (inventoryId != null && !inventoryId.isBlank()) {
            last.setInventoryId(inventoryId);
        }
        saveWorkflow(workflow);
    }

    private void saveWorkflow(OrderWorkflowEntity workflow) {
        workflow.setUpdatedAt(Instant.now());
        workflowRepository.save(workflow);
    }

    private List<PaymentStep> doneSteps(OrderWorkflowEntity workflow, String kind) {
        List<PaymentStep> steps = new ArrayList<>();
        for (WorkflowStepJson step : workflow.getSteps()) {
            if (!stepKindMatches(step.getKind(), kind) || !STEP_DONE.equals(step.getStatus())) {
                continue;
            }
            steps.add(new PaymentStep(
                    step.getEventId(),
                    step.getItemId(),
                    step.getAmount(),
                    step.getTransactionId(),
                    step.getInventoryId()));
        }
        return steps;
    }

    private static boolean stepKindMatches(String storedKind, String targetKind) {
        if (storedKind.equals(targetKind)) {
            return true;
        }
        if (STEP_CAPTURE_PAYMENT.equals(targetKind)) {
            return STEP_CAPTURE_PAYMENT_LEGACY.equals(storedKind);
        }
        if (STEP_FULFILL_LINE_ITEM.equals(targetKind)) {
            return STEP_FULFILL_LINE_ITEM_LEGACY.equals(storedKind);
        }
        return false;
    }

    private void logStep(String step, String eventId, PaymentTransferMessage res, Exception err) {
        if (err != null) {
            log.error("{} event_id={}: {}", step, eventId, err.getMessage());
            return;
        }
        if (res != null && res.error() != null && !res.error().isBlank()) {
            log.error("{} event_id={}: {}", step, eventId, res.error());
        }
    }

    private String newEventId() {
        byte[] buf = new byte[16];
        secureRandom.nextBytes(buf);
        StringBuilder sb = new StringBuilder(32);
        for (byte b : buf) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static double sellPayout(double price) {
        return Math.round(price * 50) / 100.0;
    }

    private record PaymentStep(
            String eventId, String itemId, double amount, String transactionId, String inventoryId) {
    }
}
