package com.library.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Application Configuration
 * - Caffeine cache setup
 * - JPA Auditing enabled here
 *
 * NOTE: CaffeineCacheManager works fine across versions,
 * but Caffeine builder API has minor deprecations in newer releases.
 */
@Configuration
@EnableJpaAuditing
public class AppConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(Arrays.asList("books", "users", "borrowRecords"));
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }
}
