package com.fulfillx.catalogue.exception;

public class SkuAlreadyExistsException extends RuntimeException {
    public SkuAlreadyExistsException(String message) {
        super(message);
    }
    public SkuAlreadyExistsException(){

    }
}
