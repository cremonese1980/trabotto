package com.trabotto.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.trabotto")
public class TrabottoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrabottoApplication.class, args);
    }
}
