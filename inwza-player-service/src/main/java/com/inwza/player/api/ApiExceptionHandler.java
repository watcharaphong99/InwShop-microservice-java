package com.inwza.player.api;

import com.inwza.player.api.dto.PlayerDtos;
import com.inwza.player.application.PlayerDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PlayerDomainException.class)
    public ResponseEntity<PlayerDtos.MessageResponse> domain(PlayerDomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PlayerDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PlayerDtos.MessageResponse> badRequest(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage().contains("player_id is required")
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new PlayerDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PlayerDtos.MessageResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PlayerDtos.MessageResponse(message));
    }
}
