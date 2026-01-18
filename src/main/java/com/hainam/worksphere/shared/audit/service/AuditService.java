package com.hainam.worksphere.shared.audit.service;

import com.hainam.worksphere.shared.audit.domain.AuditLog;
import com.hainam.worksphere.shared.audit.dto.AuditLogDto;
import com.hainam.worksphere.shared.audit.dto.AuditLogSearchRequest;
import com.hainam.worksphere.shared.audit.dto.AuditStatisticDto;
import com.hainam.worksphere.shared.audit.repository.AuditLogRepository;
import com.hainam.worksphere.shared.audit.config.AuditProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final AuditProperties auditProperties;

    /**
     * Create audit log
     */
    @Transactional
    public void createAuditLog(String action, String entityType, String entityId,
                              Object oldValue, Object newValue) {
        createAuditLog(action, entityType, entityId, oldValue, newValue, "SUCCESS", null);
    }

    /**
     * Create audit log with status
     */
    @Transactional
    public void createAuditLog(String action, String entityType, String entityId,
                              Object oldValue, Object newValue,
                              String status, String errorMessage) {
        createAuditLog(action, entityType, entityId, null, oldValue, newValue,
                      status, errorMessage, null);
    }

    /**
     * Create comprehensive audit log with all fields
     */
    @Transactional
    public void createAuditLog(String action, String entityType, String entityId, String fieldName,
                              Object oldValue, Object newValue,
                              String status, String errorMessage, String requestId) {
        if (!auditProperties.isEnabled()) {
            return;
        }

        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .fieldName(fieldName)
                    .oldValue(truncateValue(serializeObject(oldValue)))
                    .newValue(truncateValue(serializeObject(newValue)))
                    .status(status)
                    .errorMessage(errorMessage)
                    .requestId(requestId)
                    .build();

            enrichWithContextData(auditLog);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log for action: {}, entityType: {}, entityId: {}",
                     action, entityType, entityId, e);
        }
    }

    /**
     * Create field-level audit log
     */
    @Transactional
    public void auditField(String action, String entityType, String entityId,
                          String fieldName, Object oldValue, Object newValue, String requestId) {
        createAuditLog(action, entityType, entityId, fieldName, oldValue, newValue,
                      "SUCCESS", null, requestId);
    }

    /**
     * Create simple audit log for action only
     */
    @Transactional
    public void audit(String action) {
        createAuditLog(action, null, null, null, null);
    }

    /**
     * Create audit log for entity operation
     */
    @Transactional
    public void auditEntity(String action, String entityType, String entityId,
                           Object oldValue, Object newValue) {
        createAuditLog(action, entityType, entityId, oldValue, newValue);
    }

    /**
     * Search audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> searchAuditLogs(AuditLogSearchRequest request, Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findByCriteria(
                request.getUserId(),
                request.getAction(),
                request.getEntityType(),
                request.getStartDate(),
                request.getEndDate(),
                pageable
        );

        return auditLogs.map(this::convertToDto);
    }

    /**
     * Get audit logs by user
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsByUser(String userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get audit logs by entity
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsByEntity(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get audit statistics
     */
    @Transactional(readOnly = true)
    public List<AuditStatisticDto> getAuditStatistics(LocalDateTime startDate) {
        List<Object[]> statistics = auditLogRepository.getAuditStatistics(startDate);
        return statistics.stream()
                .map(row -> AuditStatisticDto.builder()
                        .action((String) row[0])
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get failed audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getFailedAuditLogs(Pageable pageable) {
        return auditLogRepository.findByStatusOrderByTimestampDesc("FAILED", pageable)
                .map(this::convertToDto);
    }

    /**
     * Enrich audit log with context data
     */
    private void enrichWithContextData(AuditLog auditLog) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            auditLog.setUserId(authentication.getName());
            if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                auditLog.setUsername(userDetails.getUsername());
            }
        }

        // Get HTTP request data
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setRequestMethod(request.getMethod());
            auditLog.setRequestUrl(request.getRequestURL().toString());

            // Generate request ID if not already set
            if (auditLog.getRequestId() == null) {
                auditLog.setRequestId(generateRequestId());
            }
        }

        // Set timestamp
        auditLog.setTimestamp(LocalDateTime.now());
    }

    /**
     * Generate a unique request ID
     */
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" +
               Thread.currentThread().getId();
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Serialize object to string without JSON quotes for simple values
     */
    private String serializeObject(Object obj) {
        if (obj == null) {
            return null;
        }

        // Handle simple types without JSON quotes
        if (obj instanceof String ||
            obj instanceof Number ||
            obj instanceof Boolean ||
            obj instanceof Character) {
            return obj.toString();
        }

        // Handle enums
        if (obj instanceof Enum<?>) {
            return ((Enum<?>) obj).name();
        }

        // For complex objects, use JSON serialization
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object: {}", obj.getClass().getSimpleName(), e);
            return obj.toString();
        }
    }

    /**
     * Truncate value to maximum allowed length
     */
    private String truncateValue(String value) {
        if (value == null) {
            return null;
        }

        int maxLength = auditProperties.getMaxValueLength();
        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }

    /**
     * Convert AuditLog to DTO
     */
    private AuditLogDto convertToDto(AuditLog auditLog) {
        return AuditLogDto.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .fieldName(auditLog.getFieldName())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestId(auditLog.getRequestId())
                .requestMethod(auditLog.getRequestMethod())
                .requestUrl(auditLog.getRequestUrl())
                .timestamp(auditLog.getTimestamp())
                .status(auditLog.getStatus())
                .errorMessage(auditLog.getErrorMessage())
                .build();
    }
}
