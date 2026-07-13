package com.feadev.eaids.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cache configuration for Phase 3 analytics modules.
 * Uses ConcurrentMapCache for simplicity; can be swapped to Caffeine or Redis for production.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String NATIONAL_RANKINGS_CACHE = "nationalRankings";
    public static final String BENCHMARK_PROFILES_CACHE = "benchmarkProfiles";
    public static final String POSITION_RECOMMENDATIONS_CACHE = "positionRecommendations";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new ConcurrentMapCache(NATIONAL_RANKINGS_CACHE),
                new ConcurrentMapCache(BENCHMARK_PROFILES_CACHE),
                new ConcurrentMapCache(POSITION_RECOMMENDATIONS_CACHE)
        ));
        return cacheManager;
    }
}
