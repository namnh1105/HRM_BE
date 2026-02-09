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
    public static final String DEPARTMENT_CACHE = "departments";
    public static final String EMPLOYEE_CACHE = "employees";
    public static final String ATTENDANCE_CACHE = "attendances";
    public static final String WORK_SHIFT_CACHE = "workShifts";
    public static final String CONTRACT_CACHE = "contracts";
    public static final String LEAVE_REQUEST_CACHE = "leaveRequests";
    public static final String PAYROLL_CACHE = "payrolls";
    public static final String INSURANCE_CACHE = "insurances";
    public static final String DEGREE_CACHE = "degrees";
    public static final String RELATIVE_CACHE = "relatives";

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

        // Department cache - 1 hour TTL
        cacheConfigurations.put(DEPARTMENT_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // Employee cache - 30 minutes TTL
        cacheConfigurations.put(EMPLOYEE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Attendance cache - 10 minutes TTL
        cacheConfigurations.put(ATTENDANCE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Work shift cache - 1 hour TTL
        cacheConfigurations.put(WORK_SHIFT_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // Contract cache - 30 minutes TTL
        cacheConfigurations.put(CONTRACT_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Leave request cache - 15 minutes TTL
        cacheConfigurations.put(LEAVE_REQUEST_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Payroll cache - 30 minutes TTL
        cacheConfigurations.put(PAYROLL_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Insurance cache - 30 minutes TTL
        cacheConfigurations.put(INSURANCE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Degree cache - 30 minutes TTL
        cacheConfigurations.put(DEGREE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Relative cache - 30 minutes TTL
        cacheConfigurations.put(RELATIVE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}

