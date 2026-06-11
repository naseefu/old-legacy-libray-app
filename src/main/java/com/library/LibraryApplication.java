package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Legacy Library Management System
 *
 * KNOWN ISSUES when upgrading:
 * - @EnableSwagger2 (springfox) → remove and use springdoc-openapi instead
 * - WebSecurityConfigurerAdapter removed in Spring Security 5.7+ / 6.x
 * - JWT builder/parser API completely changed in jjwt 0.11+
 * - Log4j2 must be upgraded (Log4Shell)
 * - H2 console path and config changed in newer versions
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling // ← BREAKS on Spring Boot 3.x (springfox not compatible)
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
