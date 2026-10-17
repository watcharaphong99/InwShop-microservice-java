package com.inwza.payment.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WorkflowStepJson {

    @JsonProperty("event_id")
    private String eventId;

    private String kind;

    @JsonProperty("item_id")
    private String itemId;

    private double amount;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("inventory_id")
    private String inventoryId;

    private String status;

    public WorkflowStepJson() {
    }

    public WorkflowStepJson(
            String eventId,
            String kind,
            String itemId,
            double amount,
            String transactionId,
            String inventoryId,
            String status) {
        this.eventId = eventId;
        this.kind = kind;
        this.itemId = itemId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.inventoryId = inventoryId;
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
