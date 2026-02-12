package com.hainam.worksphere.shared.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Fallback cache manager that gracefully handles Redis connection failures
 * by falling back to database queries when Redis is unavailable.
 */
@Slf4j
public class FallbackCacheManager implements CacheManager {

    private final RedisCacheManager redisCacheManager;
    private final boolean enableFallback;

    public FallbackCacheManager(RedisCacheManager redisCacheManager, boolean enableFallback) {
        this.redisCacheManager = redisCacheManager;
        this.enableFallback = enableFallback;
    }

    @Override
    public Cache getCache(String name) {
        if (!enableFallback) {
            return redisCacheManager.getCache(name);
        }

        try {
            Cache redisCache = redisCacheManager.getCache(name);
            if (redisCache != null) {
                // Test Redis connectivity by performing a simple operation
                testRedisConnectivity(redisCache);
                return new FallbackCache(redisCache, name);
            }
            return new NoOpCache(name);
        } catch (Exception e) {
            log.warn("Redis cache '{}' unavailable, falling back to database queries. Error: {}", name, e.getMessage());
            return new NoOpCache(name);
        }
    }

    @Override
    public Collection<String> getCacheNames() {
        try {
            return redisCacheManager.getCacheNames();
        } catch (Exception e) {
            log.warn("Unable to retrieve cache names from Redis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Test Redis connectivity by attempting a simple operation
     */
    private void testRedisConnectivity(Cache cache) {
        try {
            // Try to get a non-existent key to test connectivity
            cache.get("__connection_test__");
        } catch (RedisConnectionFailureException e) {
            throw new RuntimeException("Redis connection test failed", e);
        } catch (Exception e) {
            // Other exceptions are acceptable (e.g., serialization issues with test key)
            log.trace("Redis connectivity test passed with minor exception: {}", e.getMessage());
        }
    }
}
