package com.inwza.auth.application;

public class CredentialNotFoundException extends RuntimeException {

    public CredentialNotFoundException(String message) {
        super(message);
    }
}
