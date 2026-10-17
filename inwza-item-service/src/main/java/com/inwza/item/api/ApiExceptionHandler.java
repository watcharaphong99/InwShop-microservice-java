package com.inwza.item.api;

import com.inwza.item.api.dto.ItemDtos;
import com.inwza.item.application.ItemConflictException;
import com.inwza.item.application.ItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ItemDtos.MessageResponse> notFound(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ItemDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(ItemConflictException.class)
    public ResponseEntity<ItemDtos.MessageResponse> conflict(ItemConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ItemDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ItemDtos.MessageResponse> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ItemDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ItemDtos.MessageResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ItemDtos.MessageResponse(message));
    }
}
