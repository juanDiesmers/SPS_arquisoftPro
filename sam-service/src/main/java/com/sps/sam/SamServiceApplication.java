package com.sps.sam;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class SamServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SamServiceApplication.class, args);
    }
}
