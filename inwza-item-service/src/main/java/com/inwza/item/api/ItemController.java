package com.inwza.item.api;

import com.inwza.item.api.dto.ItemDtos;
import com.inwza.item.application.ItemApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/item_v1")
@Validated
public class ItemController {

    private final ItemApplicationService itemService;

    public ItemController(ItemApplicationService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("")
    public ResponseEntity<ItemDtos.MessageResponse> health() {
        return ResponseEntity.ok(new ItemDtos.MessageResponse("ok"));
    }

    @PostMapping("/item")
    public ResponseEntity<ItemDtos.ItemShowCase> create(@Valid @RequestBody ItemDtos.CreateItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(req));
    }

    @GetMapping("/item/{itemId}")
    public ItemDtos.ItemShowCase findOne(@PathVariable String itemId) {
        return itemService.findOne(stripItemPrefix(itemId));
    }

    @GetMapping("/item")
    public ItemApplicationService.GoPaginateEnvelope findMany(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String start,
            @RequestParam @Min(2) @Max(10) int limit) {
        return itemService.findManyGoShape(title, start, limit);
    }

    @PatchMapping("/item/{itemId}")
    public ItemDtos.ItemShowCase edit(
            @PathVariable String itemId,
            @Valid @RequestBody ItemDtos.ItemUpdateRequest req) {
        return itemService.edit(stripItemPrefix(itemId), req);
    }

    @PatchMapping("/item/{itemId}/is-activated")
    public boolean enableOrDisable(
            @PathVariable String itemId,
            @Valid @RequestBody ItemDtos.EnableOrDisableRequest req) {
        return itemService.enableOrDisable(stripItemPrefix(itemId), req.usageStatus());
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<ItemDtos.MessageResponse> delete(@PathVariable String itemId) {
        itemService.delete(stripItemPrefix(itemId));
        return ResponseEntity.ok(new ItemDtos.MessageResponse("deleted"));
    }

    private static String stripItemPrefix(String itemId) {
        return itemId.startsWith("item:") ? itemId : "item:" + itemId;
    }
}
