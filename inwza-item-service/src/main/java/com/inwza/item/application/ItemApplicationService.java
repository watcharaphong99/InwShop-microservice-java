package com.inwza.item.application;

import com.inwza.contracts.item.FindItemsInIdsReq;
import com.inwza.contracts.item.FindItemsInIdsRes;
import com.inwza.contracts.item.Item;
import com.inwza.item.api.dto.ItemDtos;
import com.inwza.item.infrastructure.persistence.ItemEntity;
import com.inwza.item.infrastructure.persistence.ItemJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ItemApplicationService {

    private final ItemJpaRepository repository;
    private final String nextPageBaseUrl;

    public ItemApplicationService(
            ItemJpaRepository repository,
            @Value("${inwza.paginate.item-next-page-base-url}") String nextPageBaseUrl) {
        this.repository = repository;
        this.nextPageBaseUrl = nextPageBaseUrl;
    }

    @Transactional
    public ItemDtos.ItemShowCase create(ItemDtos.CreateItemRequest req) {
        if (repository.existsByTitleIgnoreCase(req.title())) {
            throw new ItemConflictException("error: this title is already exist");
        }
        ItemEntity entity = new ItemEntity();
        entity.setTitle(req.title());
        entity.setPrice(BigDecimal.valueOf(req.price()));
        entity.setDamage(req.damage());
        entity.setImageUrl(req.imageUrl());
        entity.setUsageStatus(true);
        repository.save(entity);
        return toShowCase(entity);
    }

    @Transactional(readOnly = true)
    public ItemDtos.ItemShowCase findOne(String itemIdRaw) {
        UUID id = ItemIdCodec.parse(itemIdRaw);
        ItemEntity entity = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("error: item not found"));
        return toShowCase(entity);
    }

    @Transactional(readOnly = true)
    public ItemDtos.PaginateResponse findMany(String title, String start, int limit) {
        if (limit < 2 || limit > 10) {
            throw new IllegalArgumentException("error: limit must be between 2 and 10");
        }

        UUID afterId = null;
        if (start != null && !start.isBlank()) {
            afterId = ItemIdCodec.parse(start);
        }

        Specification<ItemEntity> spec = Specification.where(null);
        if (title != null && !title.isBlank()) {
            String pattern = "%" + title.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern));
        }
        if (afterId != null) {
            UUID cursor = afterId;
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("id"), cursor));
        }

        var page = repository.findAll(
                spec,
                PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.ASC, "id")));

        List<ItemEntity> slice = page.getContent();
        boolean hasNext = slice.size() > limit;
        List<ItemEntity> records = hasNext ? slice.subList(0, limit) : slice;

        List<ItemDtos.ItemShowCase> data = records.stream().map(this::toShowCase).toList();
        long total = repository.count(spec);

        String firstHref = nextPageBaseUrl + "?limit=" + limit;
        if (title != null && !title.isBlank()) {
            firstHref += "&title=" + title;
        }

        String nextStart = "";
        String nextHref = "";
        if (hasNext && !records.isEmpty()) {
            nextStart = ItemIdCodec.toExternal(records.getLast().getId());
            nextHref = firstHref + "&start=" + nextStart;
        }

        return new ItemDtos.PaginateResponse(
                total,
                data,
                nextHref.isEmpty() ? null : nextHref
        );
    }

    // Align JSON with Go PaginateRes shape
    @Transactional(readOnly = true)
    public GoPaginateEnvelope findManyGoShape(String title, String start, int limit) {
        ItemDtos.PaginateResponse page = findMany(title, start, limit);
        String firstHref = nextPageBaseUrl + "?limit=" + limit;
        if (title != null && !title.isBlank()) {
            firstHref += "&title=" + title;
        }
        GoPaginateEnvelope.NextLink next = new GoPaginateEnvelope.NextLink("", "");
        if (page.next() != null && !page.next().isBlank() && !page.records().isEmpty()) {
            String nextStart = page.records().getLast().itemId();
            next = new GoPaginateEnvelope.NextLink(nextStart, page.next());
        }
        return new GoPaginateEnvelope(
                page.records(),
                limit,
                page.total(),
                new GoPaginateEnvelope.FirstLink(firstHref),
                next
        );
    }

    @Transactional
    public ItemDtos.ItemShowCase edit(String itemIdRaw, ItemDtos.ItemUpdateRequest req) {
        UUID id = ItemIdCodec.parse(itemIdRaw);
        ItemEntity entity = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("error: item not found"));

        if (req.title() != null && !req.title().isBlank()) {
            repository.findByTitleIgnoreCase(req.title())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(x -> {
                        throw new ItemConflictException("error: this title is already exist");
                    });
            entity.setTitle(req.title());
        }
        if (req.price() != null) {
            entity.setPrice(BigDecimal.valueOf(req.price()));
        }
        if (req.imageUrl() != null && !req.imageUrl().isBlank()) {
            entity.setImageUrl(req.imageUrl());
        }
        if (req.damage() != null) {
            entity.setDamage(req.damage());
        }
        return toShowCase(repository.save(entity));
    }

    @Transactional
    public boolean enableOrDisable(String itemIdRaw, boolean usageStatus) {
        UUID id = ItemIdCodec.parse(itemIdRaw);
        ItemEntity entity = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("error: item not found"));
        entity.setUsageStatus(usageStatus);
        repository.save(entity);
        return usageStatus;
    }

    @Transactional
    public long delete(String itemIdRaw) {
        UUID id = ItemIdCodec.parse(itemIdRaw);
        if (!repository.existsById(id)) {
            throw new ItemNotFoundException("error: item not found");
        }
        repository.deleteById(id);
        return 1L;
    }

    @Transactional(readOnly = true)
    public FindItemsInIdsRes findItemsInIds(FindItemsInIdsReq request) {
        List<UUID> ids = new ArrayList<>();
        for (String raw : request.getIdsList()) {
            ids.add(ItemIdCodec.parse(raw));
        }
        List<ItemEntity> entities = repository.findByIdInAndUsageStatusTrue(ids);
        FindItemsInIdsRes.Builder builder = FindItemsInIdsRes.newBuilder();
        for (ItemEntity entity : entities) {
            builder.addItems(Item.newBuilder()
                    .setId(ItemIdCodec.toExternal(entity.getId()))
                    .setTitle(entity.getTitle())
                    .setPrice(entity.getPrice().doubleValue())
                    .setImageUrl(entity.getImageUrl())
                    .setDamage(entity.getDamage())
                    .build());
        }
        return builder.build();
    }

    private ItemDtos.ItemShowCase toShowCase(ItemEntity entity) {
        return new ItemDtos.ItemShowCase(
                ItemIdCodec.toExternal(entity.getId()),
                entity.getTitle(),
                entity.getPrice().doubleValue(),
                entity.getDamage(),
                entity.getImageUrl()
        );
    }

    public record GoPaginateEnvelope(
            Object data,
            int limit,
            long total,
            GoPaginateEnvelope.FirstLink first,
            GoPaginateEnvelope.NextLink next
    ) {
        public record FirstLink(String href) {
        }

        public record NextLink(String start, String href) {
        }
    }
}
