package com.elcom.ss_api_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // <-- THIS TURNS ON THE MASTER ALARM CLOCK
public class SsApiJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsApiJavaApplication.class, args);
    }
}