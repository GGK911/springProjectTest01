package com.ggk911.springtest01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class SpringTest01Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringTest01Application.class, args);
    }

}
