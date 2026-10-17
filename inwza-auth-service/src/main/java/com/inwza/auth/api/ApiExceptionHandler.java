package com.inwza.auth.api;

import com.inwza.auth.api.dto.AuthDtos;
import com.inwza.auth.application.AuthFailureException;
import com.inwza.auth.application.CredentialNotFoundException;
import com.inwza.auth.application.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthFailureException.class)
    public ResponseEntity<AuthDtos.MessageResponse> unauthorized(AuthFailureException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler({InvalidTokenException.class, IllegalArgumentException.class})
    public ResponseEntity<AuthDtos.MessageResponse> badRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<AuthDtos.MessageResponse> notFound(CredentialNotFoundException ex) {
        if (ex.getMessage().contains("player credential not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AuthDtos.MessageResponse(ex.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthDtos.MessageResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthDtos.MessageResponse(message));
    }
}
