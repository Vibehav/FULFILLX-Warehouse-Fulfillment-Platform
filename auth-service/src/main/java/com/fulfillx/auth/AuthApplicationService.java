package com.fulfillx.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// Scan all components in the package, Auto-configure everything, Start the embedded Tomcat server
public class AuthApplicationService {
    public static void main(String[] args){
        SpringApplication.run(AuthApplicationService.class,args);
    }
}
