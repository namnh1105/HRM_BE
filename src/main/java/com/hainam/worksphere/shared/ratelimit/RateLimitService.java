package com.hainam.worksphere.shared.ratelimit;

import com.hainam.worksphere.shared.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RateLimitProperties rateLimitProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    // Local bucket cache for performance - tokens are tracked in Redis for distributed consistency
    private final Map<String, Bucket> localBucketCache = new ConcurrentHashMap<>();

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final String VIOLATION_KEY_PREFIX = "rate_limit_violation:";
    private static final String BAN_KEY_PREFIX = "rate_limit_ban:";
    private static final String REQUEST_COUNT_PREFIX = "rate_limit_count:";

    public boolean isAllowed(String key, RateLimitType type) {
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        if (isBanned(key)) {
            log.warn("Rate limit: Key {} is currently banned", key);
            return false;
        }

        // Use Redis-based sliding window rate limiting for distributed consistency
        boolean allowed = isAllowedByRedis(key, type);

        if (!allowed) {
            recordViolation(key);
            log.warn("Rate limit exceeded for key: {} type: {}", key, type);
        }

        return allowed;
    }

    /**
     * Redis-based sliding window rate limiting for distributed environment
     */
    private boolean isAllowedByRedis(String key, RateLimitType type) {
        String redisKey = REQUEST_COUNT_PREFIX + type.name() + ":" + key;
        int limit = getLimit(type);
        long windowSizeInSeconds = 60; // 1 minute window

        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey, 1);

            if (currentCount == null) {
                return true; // Redis error, allow request
            }

            if (currentCount == 1) {
                // First request in window, set expiration
                redisTemplate.expire(redisKey, windowSizeInSeconds, TimeUnit.SECONDS);
            }

            return currentCount <= limit;
        } catch (Exception e) {
            log.error("Redis error during rate limit check for key: {}, falling back to local bucket", key, e);
            // Fallback to local bucket if Redis fails
            return isAllowedByLocalBucket(key, type);
        }
    }

    /**
     * Fallback: Local bucket-based rate limiting when Redis is unavailable
     */
    private boolean isAllowedByLocalBucket(String key, RateLimitType type) {
        Bucket bucket = getBucket(key, type);
        return bucket.tryConsume(1);
    }

    public long getAvailableTokens(String key, RateLimitType type) {
        String redisKey = REQUEST_COUNT_PREFIX + type.name() + ":" + key;
        int limit = getLimit(type);

        try {
            Object countObj = redisTemplate.opsForValue().get(redisKey);
            if (countObj == null) {
                return limit;
            }
            long currentCount = ((Number) countObj).longValue();
            return Math.max(0, limit - currentCount);
        } catch (Exception e) {
            log.error("Failed to get available tokens from Redis for key: {}", key, e);
            // Fallback to local bucket
            Bucket bucket = getBucket(key, type);
            return bucket.getAvailableTokens();
        }
    }

    private int getLimit(RateLimitType type) {
        return switch (type) {
            case LOGIN -> rateLimitProperties.getLoginRequestsPerMinute();
            case REGISTER -> rateLimitProperties.getRegisterRequestsPerMinute();
            case REFRESH_TOKEN -> rateLimitProperties.getRefreshTokenRequestsPerMinute();
            case ANONYMOUS -> rateLimitProperties.getAnonymousRequestsPerMinute();
            case AUTHENTICATED -> rateLimitProperties.getDefaultRequestsPerMinute();
        };
    }

    private Bucket getBucket(String key, RateLimitType type) {
        String cacheKey = RATE_LIMIT_KEY_PREFIX + type.name() + ":" + key;
        return localBucketCache.computeIfAbsent(cacheKey, k -> createBucket(type));
    }

    private Bucket createBucket(RateLimitType type) {
        int requestsPerMinute = getLimit(type);

        Bandwidth limit = Bandwidth.classic(
            requestsPerMinute,
            Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))
        );

        return Bucket.builder().addLimit(limit).build();
    }

    private void recordViolation(String key) {
        String violationKey = VIOLATION_KEY_PREFIX + key;

        try {
            Long violations = redisTemplate.opsForValue().increment(violationKey, 1);

            if (violations == null) {
                return;
            }

            if (violations == 1) {
                // Set expiration for violation tracking (e.g., 1 hour)
                redisTemplate.expire(violationKey, 1, TimeUnit.HOURS);
            }

            if (violations >= rateLimitProperties.getMaxViolationsBeforeBan()) {
                banKey(key);
                redisTemplate.delete(violationKey);
            }
        } catch (Exception e) {
            log.error("Failed to record violation in Redis for key: {}", key, e);
        }
    }

    private void banKey(String key) {
        String banKey = BAN_KEY_PREFIX + key;
        Duration banDuration = Duration.ofMinutes(rateLimitProperties.getBanDurationMinutes());

        try {
            redisTemplate.opsForValue().set(banKey, "banned", banDuration);
            log.warn("Rate limit: Key {} has been banned for {} minutes", key, rateLimitProperties.getBanDurationMinutes());
        } catch (Exception e) {
            log.error("Failed to set ban in Redis for key: {}", key, e);
        }
    }

    private boolean isBanned(String key) {
        String banKey = BAN_KEY_PREFIX + key;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(banKey));
        } catch (Exception e) {
            log.error("Failed to check ban status in Redis for key: {}", key, e);
            return false;
        }
    }

    public void resetRateLimit(String key) {
        localBucketCache.entrySet().removeIf(entry -> entry.getKey().contains(key));

        try {
            // Delete all related Redis keys
            redisTemplate.delete(BAN_KEY_PREFIX + key);
            redisTemplate.delete(VIOLATION_KEY_PREFIX + key);

            // Delete rate limit counters for all types
            for (RateLimitType type : RateLimitType.values()) {
                redisTemplate.delete(REQUEST_COUNT_PREFIX + type.name() + ":" + key);
            }

            log.info("Rate limit reset for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to reset rate limit in Redis for key: {}", key, e);
        }
    }

    public void clearAllRateLimits() {
        localBucketCache.clear();

        try {
            // Clear all rate limit related keys from Redis
            Set<String> rateLimitKeys = redisTemplate.keys(RATE_LIMIT_KEY_PREFIX + "*");
            Set<String> violationKeys = redisTemplate.keys(VIOLATION_KEY_PREFIX + "*");
            Set<String> banKeys = redisTemplate.keys(BAN_KEY_PREFIX + "*");
            Set<String> countKeys = redisTemplate.keys(REQUEST_COUNT_PREFIX + "*");

            if (rateLimitKeys != null && !rateLimitKeys.isEmpty()) {
                redisTemplate.delete(rateLimitKeys);
            }
            if (violationKeys != null && !violationKeys.isEmpty()) {
                redisTemplate.delete(violationKeys);
            }
            if (banKeys != null && !banKeys.isEmpty()) {
                redisTemplate.delete(banKeys);
            }
            if (countKeys != null && !countKeys.isEmpty()) {
                redisTemplate.delete(countKeys);
            }

            log.info("All rate limits cleared");
        } catch (Exception e) {
            log.error("Failed to clear all rate limits from Redis", e);
        }
    }

    /**
     * Get remaining ban time in seconds for a key
     */
    public long getRemainingBanTime(String key) {
        String banKey = BAN_KEY_PREFIX + key;
        try {
            Long ttl = redisTemplate.getExpire(banKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.error("Failed to get ban time from Redis for key: {}", key, e);
            return 0;
        }
    }

    /**
     * Manually unban a key
     */
    public void unbanKey(String key) {
        String banKey = BAN_KEY_PREFIX + key;
        try {
            redisTemplate.delete(banKey);
            log.info("Key {} has been unbanned", key);
        } catch (Exception e) {
            log.error("Failed to unban key in Redis: {}", key, e);
        }
    }
}
