package com.fulfillx.inbound.exception;

public class InvalidGRNStateException extends RuntimeException{
    public InvalidGRNStateException(String message){
        super(message);
    }
}
