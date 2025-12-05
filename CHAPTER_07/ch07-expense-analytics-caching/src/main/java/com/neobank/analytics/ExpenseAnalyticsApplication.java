package com.neobank.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ExpenseAnalyticsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ExpenseAnalyticsApplication.class, args);
    }
}
