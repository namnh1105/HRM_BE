package com.hainam.worksphere.shared.ratelimit;

import com.hainam.worksphere.shared.config.RateLimitProperties;
import com.hainam.worksphere.shared.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Aspect to handle @RateLimit annotation for method-level rate limiting
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    @Around("@annotation(com.hainam.worksphere.shared.ratelimit.RateLimit)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!rateLimitProperties.isEnabled()) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        String key = buildRateLimitKey(rateLimit, method);

        if (!rateLimitService.isAllowed(key, rateLimit.type())) {
            long remainingBanTime = rateLimitService.getRemainingBanTime(key);
            String message = remainingBanTime > 0
                    ? String.format("Rate limit exceeded. You are banned for %d more seconds.", remainingBanTime)
                    : "Rate limit exceeded. Please try again later.";

            log.warn("Rate limit exceeded for method {} - Key: {}", method.getName(), key);
            throw new RateLimitExceededException(message, remainingBanTime);
        }

        return joinPoint.proceed();
    }

    private String buildRateLimitKey(RateLimit rateLimit, Method method) {
        StringBuilder keyBuilder = new StringBuilder();

        // Add custom prefix if provided
        if (!rateLimit.keyPrefix().isEmpty()) {
            keyBuilder.append(rateLimit.keyPrefix()).append(":");
        } else {
            keyBuilder.append(method.getDeclaringClass().getSimpleName())
                    .append(":")
                    .append(method.getName())
                    .append(":");
        }

        // Add user ID if configured
        if (rateLimit.includeUserId()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                keyBuilder.append("user:").append(auth.getName()).append(":");
            }
        }

        // Add IP address if configured
        if (rateLimit.includeIp()) {
            String clientIp = getClientIP();
            if (clientIp != null) {
                keyBuilder.append("ip:").append(clientIp);
            }
        }

        return keyBuilder.toString();
    }

    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}

