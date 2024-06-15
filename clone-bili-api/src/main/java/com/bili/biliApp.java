package com.bili;

import Service.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
@EnableScheduling
public class biliApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(biliApp.class, args);
        WebSocketService.setApplicationContext(app); //全局变量
    }
}
