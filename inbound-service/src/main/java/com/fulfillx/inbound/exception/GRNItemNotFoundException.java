package com.fulfillx.inbound.exception;

public class GRNItemNotFoundException extends RuntimeException{

    public GRNItemNotFoundException(String message){
        super(message);
    }
}
