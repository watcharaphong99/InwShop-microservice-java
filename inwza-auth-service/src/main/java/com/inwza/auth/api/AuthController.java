package com.inwza.auth.api;

import com.inwza.auth.api.dto.AuthDtos;
import com.inwza.auth.application.AuthApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth_v1")
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @GetMapping("")
    public ResponseEntity<AuthDtos.MessageResponse> health() {
        return ResponseEntity.ok(new AuthDtos.MessageResponse("ok"));
    }

    @PostMapping("/auth/login")
    public AuthDtos.ProfileInterceptorResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/auth/refresh-token")
    public AuthDtos.ProfileInterceptorResponse refresh(@Valid @RequestBody AuthDtos.RefreshTokenRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<AuthDtos.MessageResponse> logout(@Valid @RequestBody AuthDtos.LogoutRequest req) {
        long deleted = authService.logout(req.credentialId());
        return ResponseEntity.ok(new AuthDtos.MessageResponse("Deleted count: " + deleted));
    }
}
