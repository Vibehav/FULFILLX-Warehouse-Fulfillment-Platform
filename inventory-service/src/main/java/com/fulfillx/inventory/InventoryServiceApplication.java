package com.fulfillx.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableCaching
@EnableRetry
@ComponentScan(basePackages = {
        "com.fulfillx.inventory",
        "com.fulfillx.common"
})
public class InventoryServiceApplication {
    public static void main(String[] args) {

        SpringApplication.run(InventoryServiceApplication.class,args);
    }
}
