package com.inwza.payment.api;

import com.inwza.payment.api.dto.PaymentDtos;
import com.inwza.payment.application.PaymentDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PaymentDomainException.class)
    public ResponseEntity<PaymentDtos.MessageResponse> domain(PaymentDomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PaymentDtos.MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PaymentDtos.MessageResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PaymentDtos.MessageResponse(message));
    }
}
