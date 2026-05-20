package com.fulfillx.catalogue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.fulfillx.catalogue",
        "com.fulfillx.common"
})
public class CatalogueServiceApplication {

    public static void main(String[] args)
    {
//        SpringApplication.run(CatalogueServiceApplication.class,args);
        var ctx = SpringApplication.run(CatalogueServiceApplication.class, args);
        System.out.println(">>> JwtAuthFilter bean exists: " +
                ctx.containsBean("jwtAuthFilter"));
        System.out.println(">>> JwtUtil bean exists: " +
                ctx.containsBean("jwtUtil"));
    }
}
