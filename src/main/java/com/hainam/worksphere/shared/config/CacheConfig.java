package com.hainam.worksphere.shared.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USER_CACHE = "users";
    public static final String USER_BY_EMAIL_CACHE = "usersByEmail";
    public static final String ROLE_CACHE = "roles";
    public static final String ROLE_BY_CODE_CACHE = "rolesByCode";
    public static final String PERMISSION_CACHE = "permissions";
    public static final String PERMISSION_BY_CODE_CACHE = "permissionsByCode";
    public static final String USER_ROLES_CACHE = "userRoles";
    public static final String USER_PERMISSIONS_CACHE = "userPermissions";
    public static final String ROLE_PERMISSIONS_CACHE = "rolePermissions";
    public static final String ACTIVE_ROLES_CACHE = "activeRoles";
    public static final String ACTIVE_PERMISSIONS_CACHE = "activePermissions";
    public static final String SYSTEM_ROLES_CACHE = "systemRoles";
    public static final String SYSTEM_PERMISSIONS_CACHE = "systemPermissions";

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User cache - 30 minutes TTL
        cacheConfigurations.put(USER_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(USER_BY_EMAIL_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Role cache - 1 hour TTL (roles change less frequently)
        cacheConfigurations.put(ROLE_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(ROLE_BY_CODE_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // Permission cache - 1 hour TTL (permissions change less frequently)
        cacheConfigurations.put(PERMISSION_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(PERMISSION_BY_CODE_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // User roles and permissions cache - 15 minutes TTL (authorization data)
        cacheConfigurations.put(USER_ROLES_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(USER_PERMISSIONS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(ROLE_PERMISSIONS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Active and System cache - 1 hour TTL (rarely changes)
        cacheConfigurations.put(ACTIVE_ROLES_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(ACTIVE_PERMISSIONS_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(SYSTEM_ROLES_CACHE, defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put(SYSTEM_PERMISSIONS_CACHE, defaultConfig.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}

