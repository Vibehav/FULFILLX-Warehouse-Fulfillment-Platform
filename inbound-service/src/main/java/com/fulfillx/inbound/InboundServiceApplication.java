package com.fulfillx.inbound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages =
        {"com.fulfillx.inbound",
        "com.fulfillx.common"
}
)
public class InboundServiceApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(InboundServiceApplication.class,args);
        System.out.println(">>> JwtAuthFilter bean exists: " +
                ctx.containsBean("jwtAuthFilter"));
        System.out.println(">>> JwtUtil bean exists: " +
                ctx.containsBean("jwtUtil"));
    }
}
