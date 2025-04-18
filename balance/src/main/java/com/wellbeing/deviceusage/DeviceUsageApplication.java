package com.wellbeing.deviceusage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeviceUsageApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceUsageApplication.class, args);
    }
}