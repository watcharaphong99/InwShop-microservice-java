package com.inwza.inventory.api;

import com.inwza.inventory.api.dto.InventoryDtos;
import com.inwza.inventory.application.InventoryApplicationService;
import com.inwza.inventory.application.PlayerIdCodec;
import com.inwza.inventory.config.PlayerRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory_v1")
@Validated
public class InventoryController {

    private final InventoryApplicationService inventoryService;

    public InventoryController(InventoryApplicationService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("")
    public ResponseEntity<InventoryDtos.MessageResponse> health() {
        return ResponseEntity.ok(new InventoryDtos.MessageResponse("ok"));
    }

    @GetMapping("/inventory/{playerId}")
    public InventoryDtos.PaginateResponse findPlayerItems(
            HttpServletRequest request,
            @PathVariable String playerId,
            @RequestParam(required = false) String start,
            @RequestParam @Min(2) @Max(10) int limit) {
        validatePlayerPath(request, playerId);
        return inventoryService.findPlayerItems(playerId, start, limit);
    }

    private static void validatePlayerPath(HttpServletRequest request, String pathPlayerId) {
        Object tokenPlayer = request.getAttribute(PlayerRequestAttributes.PLAYER_ID);
        if (!(tokenPlayer instanceof String token) || token.isBlank()) {
            throw new IllegalArgumentException("error: player_id is required");
        }
        String normalizedPath = PlayerIdCodec.format(pathPlayerId);
        String normalizedToken = PlayerIdCodec.format(token);
        if (!normalizedPath.equals(normalizedToken)) {
            throw new IllegalArgumentException("error: player id is invalid");
        }
    }
}
