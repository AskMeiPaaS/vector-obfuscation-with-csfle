package com.ayedata.vectorobfuscation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VectorObfuscationApplication {

    public static void main(String[] args) {
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "10"); // Optional tuning
        SpringApplication.run(VectorObfuscationApplication.class, args);
    }
}
