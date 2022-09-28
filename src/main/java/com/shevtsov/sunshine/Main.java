package com.shevtsov.sunshine;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.shevtsov.sunshine")
@EnableTransactionManagement
public class Main {
    public static void main(String[] args) {
        log.info("Application stated!");
        SpringApplication.run(Main.class, args);
    }
}
