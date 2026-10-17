package com.inwza.auth.application;

public class AuthFailureException extends RuntimeException {

    public AuthFailureException(String message) {
        super(message);
    }
}
