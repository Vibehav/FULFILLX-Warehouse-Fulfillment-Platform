package com.fulfillx.catalogue.exception;

import com.fulfillx.catalogue.entity.Sku;

public class SkuNotFoundException extends RuntimeException{

    public SkuNotFoundException(String message){
        super(message);
    }
}
