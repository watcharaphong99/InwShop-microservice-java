package com.inwza.inventory.api;

import com.inwza.inventory.api.dto.InventoryDtos;
import com.inwza.inventory.application.InventoryDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InventoryDomainException.class)
    public ResponseEntity<InventoryDtos.MessageResponse> domain(InventoryDomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InventoryDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<InventoryDtos.MessageResponse> badRequest(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage().contains("player")
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new InventoryDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<InventoryDtos.MessageResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InventoryDtos.MessageResponse(message));
    }
}
