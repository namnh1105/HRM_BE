package com.hainam.worksphere.shared.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hainam.worksphere.shared.config.RateLimitProperties;
import com.hainam.worksphere.shared.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        RateLimitType rateLimitType = resolveRateLimitType(request);

        if (!rateLimitService.isAllowed(clientKey, rateLimitType)) {
            sendRateLimitExceededResponse(response, clientKey, rateLimitType);
            return;
        }

        addRateLimitHeaders(response, clientKey, rateLimitType);
        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }

        return "ip:" + getClientIP(request);
    }

    private String getClientIP(HttpServletRequest request) {
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

    private RateLimitType resolveRateLimitType(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.contains("/auth/login")) return RateLimitType.LOGIN;
        if (path.contains("/auth/register")) return RateLimitType.REGISTER;
        if (path.contains("/auth/refresh")) return RateLimitType.REFRESH_TOKEN;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return RateLimitType.AUTHENTICATED;
        }

        return RateLimitType.ANONYMOUS;
    }

    private void sendRateLimitExceededResponse(HttpServletResponse response,
                                                String clientKey,
                                                RateLimitType rateLimitType) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Add Retry-After header
        long remainingBanTime = rateLimitService.getRemainingBanTime(clientKey);
        int retryAfterSeconds = remainingBanTime > 0 ? (int) remainingBanTime : 60;
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        String message = remainingBanTime > 0
                ? String.format("Rate limit exceeded. You are banned for %d more seconds.", remainingBanTime)
                : "Rate limit exceeded. Please try again later.";

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Too Many Requests")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        log.warn("Rate limit exceeded - Key: {}, Type: {}", clientKey, rateLimitType);
    }

    private void addRateLimitHeaders(HttpServletResponse response, String clientKey, RateLimitType rateLimitType) {
        long availableTokens = rateLimitService.getAvailableTokens(clientKey, rateLimitType);
        int limit = getLimit(rateLimitType);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
        response.setHeader("X-RateLimit-Reset", "60");
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico")
                || path.startsWith("/actuator/health")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
