package com.fulfillx.inbound.exception;

public class GRNNotFoundException extends RuntimeException {
    public GRNNotFoundException(String message) {
        super(message);
    }
}
