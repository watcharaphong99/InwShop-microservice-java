package com.inwza.payment.application.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class PaymentReplyRegistry {

    private static final Logger log = LoggerFactory.getLogger(PaymentReplyRegistry.class);

    private final ConcurrentHashMap<String, BlockingQueue<PaymentTransferMessage>> waiters =
            new ConcurrentHashMap<>();

    public BlockingQueue<PaymentTransferMessage> register(String eventId) {
        BlockingQueue<PaymentTransferMessage> queue = new ArrayBlockingQueue<>(1);
        waiters.put(eventId, queue);
        return queue;
    }

    public void unregister(String eventId) {
        waiters.remove(eventId);
    }

    public void accept(PaymentTransferMessage message) {
        if (message == null || message.eventId() == null || message.eventId().isBlank()) {
            log.error("payment reply missing event_id");
            return;
        }
        BlockingQueue<PaymentTransferMessage> queue = waiters.get(message.eventId());
        if (queue == null) {
            log.info("payment reply event_id={} has no waiter", message.eventId());
            return;
        }
        if (!queue.offer(message)) {
            log.error("payment reply event_id={} dropped", message.eventId());
        }
    }

    public PaymentTransferMessage waitReply(String eventId, BlockingQueue<PaymentTransferMessage> queue, long timeoutMs)
            throws InterruptedException {
        PaymentTransferMessage res = queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (res == null) {
            throw new PaymentReplyException("error: payment step timeout");
        }
        if (!eventId.equals(res.eventId())) {
            throw new PaymentReplyException("error: payment reply mismatch");
        }
        return res;
    }

    public static class PaymentReplyException extends RuntimeException {
        public PaymentReplyException(String message) {
            super(message);
        }
    }
}
