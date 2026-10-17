package com.inwza.inventory.application;

import com.inwza.contracts.item.FindItemsInIdsRes;
import com.inwza.contracts.item.Item;
import com.inwza.inventory.api.dto.InventoryDtos;
import com.inwza.inventory.application.messaging.PaymentReplyPublisher;
import com.inwza.inventory.application.messaging.PaymentTransferMessage;
import com.inwza.inventory.application.messaging.RollbackInventoryMessage;
import com.inwza.inventory.application.messaging.UpdateInventoryMessage;
import com.inwza.inventory.config.PaginateProperties;
import com.inwza.inventory.infrastructure.grpc.ItemGrpcGateway;
import com.inwza.inventory.infrastructure.persistence.PlayerInventoryEntity;
import com.inwza.inventory.infrastructure.persistence.PlayerInventoryJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InventoryApplicationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryApplicationService.class);

    private final PlayerInventoryJpaRepository inventoryRepo;
    private final InventoryDomainService domain;
    private final ItemGrpcGateway itemGrpc;
    private final PaymentReplyPublisher paymentReplies;
    private final PaginateProperties paginate;

    public InventoryApplicationService(
            PlayerInventoryJpaRepository inventoryRepo,
            InventoryDomainService domain,
            ItemGrpcGateway itemGrpc,
            PaymentReplyPublisher paymentReplies,
            PaginateProperties paginate) {
        this.inventoryRepo = inventoryRepo;
        this.domain = domain;
        this.itemGrpc = itemGrpc;
        this.paymentReplies = paymentReplies;
        this.paginate = paginate;
    }

    @Transactional(readOnly = true)
    public InventoryDtos.PaginateResponse findPlayerItems(String playerIdRaw, String start, int limit) {
        if (limit < 2 || limit > 10) {
            throw new InventoryDomainException("error: limit must be between 2 and 10");
        }
        String playerId = PlayerIdCodec.format(playerIdRaw);
        UUID afterId = null;
        if (start != null && !start.isBlank()) {
            afterId = PlayerIdCodec.parseInventoryId(start);
        }

        List<PlayerInventoryEntity> rows = inventoryRepo.findActivePage(
                playerId, afterId, PageRequest.of(0, limit));
        if (rows.isEmpty()) {
            String firstHref = baseHref(playerId, limit);
            return new InventoryDtos.PaginateResponse(
                    List.of(), 0, limit,
                    new InventoryDtos.FirstLink(firstHref),
                    new InventoryDtos.NextLink("", ""));
        }

        List<String> itemIds = rows.stream().map(PlayerInventoryEntity::getItemId).distinct().toList();
        FindItemsInIdsRes catalog = itemGrpc.findItemsInIds(itemIds);
        Map<String, Item> itemMap = new HashMap<>();
        for (Item item : catalog.getItemsList()) {
            itemMap.put(item.getId(), item);
        }

        List<InventoryDtos.ItemInInventory> data = new ArrayList<>();
        for (PlayerInventoryEntity row : rows) {
            Item item = itemMap.get(row.getItemId());
            if (item == null) {
                continue;
            }
            data.add(new InventoryDtos.ItemInInventory(
                    row.getId().toString(),
                    row.getPlayerId(),
                    new InventoryDtos.ItemShowCase(
                            item.getId(),
                            item.getTitle(),
                            item.getPrice(),
                            item.getDamage(),
                            item.getImageUrl())));
        }

        long total = inventoryRepo.countByPlayerIdAndRemovedEventIdIsNull(playerId);
        String firstHref = baseHref(playerId, limit);
        String nextStart = rows.getLast().getId().toString();
        String nextHref = firstHref + "&start=" + nextStart;

        return new InventoryDtos.PaginateResponse(
                data,
                total,
                limit,
                new InventoryDtos.FirstLink(firstHref),
                new InventoryDtos.NextLink(nextStart, nextHref));
    }

    public void addPlayerItemRes(UpdateInventoryMessage req) {
        if (req.eventId() == null || req.eventId().isBlank()) {
            replyAdd(req, "", "error: event_id is required");
            return;
        }
        try {
            UUID inventoryId = domain.insertItem(req.playerId(), req.itemId(), req.eventId());
            replyAdd(req, inventoryId.toString(), "");
        } catch (InventoryDomainException ex) {
            replyAdd(req, "", ex.getMessage());
        }
    }

    public void removePlayerItemRes(UpdateInventoryMessage req) {
        if (req.eventId() == null || req.eventId().isBlank()) {
            replyRemove(req, "error: event_id is required");
            return;
        }
        try {
            domain.removeByEvent(req.playerId(), req.itemId(), req.eventId());
            replyRemove(req, "");
        } catch (InventoryDomainException ex) {
            replyRemove(req, ex.getMessage());
        }
    }

    public void rollbackAdd(RollbackInventoryMessage req) {
        try {
            domain.rollbackAdded(req.inventoryId(), req.eventId());
        } catch (Exception ex) {
            log.error("RollbackAddPlayerItem: {}", ex.getMessage());
        }
    }

    public void rollbackRemove(RollbackInventoryMessage req) {
        try {
            domain.rollbackRemoved(req.eventId());
        } catch (Exception ex) {
            log.error("RollbackRemovePlayerItem: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public boolean isAvailableToSell(String playerId, String itemId) {
        return domain.hasActiveItem(playerId, itemId);
    }

    private void replyAdd(UpdateInventoryMessage req, String inventoryId, String error) {
        paymentReplies.publishBuyReply(new PaymentTransferMessage(
                req.eventId(),
                inventoryId,
                null,
                PlayerIdCodec.format(req.playerId()),
                req.itemId(),
                0,
                error == null || error.isBlank() ? null : error));
    }

    private void replyRemove(UpdateInventoryMessage req, String error) {
        paymentReplies.publishSellReply(new PaymentTransferMessage(
                req.eventId(),
                null,
                null,
                PlayerIdCodec.format(req.playerId()),
                req.itemId(),
                0,
                error == null || error.isBlank() ? null : error));
    }

    private String baseHref(String playerId, int limit) {
        return paginate.inventoryNextPageBaseUrl() + "/" + PlayerIdCodec.stripPrefix(playerId) + "?limit=" + limit;
    }
}
