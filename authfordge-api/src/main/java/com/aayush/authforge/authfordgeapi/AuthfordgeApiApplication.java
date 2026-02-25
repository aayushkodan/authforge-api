package com.aayush.authforge.authfordgeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AuthfordgeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthfordgeApiApplication.class, args);
    }

}
