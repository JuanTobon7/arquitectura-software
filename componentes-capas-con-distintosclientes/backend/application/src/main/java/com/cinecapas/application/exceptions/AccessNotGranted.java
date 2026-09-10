package com.cinecapas.application.exceptions;

public class AccessNotGranted extends RuntimeException {
    public AccessNotGranted(String message) {
        super(message);
    }
}
