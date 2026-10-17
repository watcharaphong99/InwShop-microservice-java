package com.inwza.player.api;

import com.inwza.player.api.dto.PlayerDtos;
import com.inwza.player.application.PlayerApplicationService;
import com.inwza.player.application.PlayerIdCodec;
import com.inwza.player.config.PlayerRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/player_v1")
public class PlayerController {

    private final PlayerApplicationService playerService;

    public PlayerController(PlayerApplicationService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("")
    public ResponseEntity<PlayerDtos.MessageResponse> health() {
        return ResponseEntity.ok(new PlayerDtos.MessageResponse("ok"));
    }

    @PostMapping("/player/register")
    public ResponseEntity<PlayerDtos.PlayerProfileResponse> register(@Valid @RequestBody PlayerDtos.CreatePlayerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.createPlayer(req));
    }

    @GetMapping("/player/{playerId}")
    public PlayerDtos.PlayerProfileResponse profile(@PathVariable String playerId) {
        return playerService.findProfile(stripPrefix(playerId));
    }

    @PostMapping("/player/add-money")
    public ResponseEntity<PlayerDtos.SavingAccountResponse> addMoney(
            HttpServletRequest request, @Valid @RequestBody PlayerDtos.AddMoneyRequest body) {
        String playerId = requiredPlayerId(request);
        PlayerDtos.AddMoneyRequest req = new PlayerDtos.AddMoneyRequest(playerId, body.amount(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.addMoney(req));
    }

    @GetMapping("/player/saving-account/my-account")
    public PlayerDtos.SavingAccountResponse myAccount(HttpServletRequest request) {
        return playerService.getSavingAccount(requiredPlayerId(request));
    }

    private static String requiredPlayerId(HttpServletRequest request) {
        Object attr = request.getAttribute(PlayerRequestAttributes.PLAYER_ID);
        if (attr instanceof String s && !s.isBlank()) {
            return PlayerIdCodec.format(s);
        }
        throw new IllegalArgumentException("error: player_id is required");
    }

    private static String stripPrefix(String playerId) {
        return playerId.startsWith("player:") ? playerId.substring("player:".length()) : playerId;
    }
}
