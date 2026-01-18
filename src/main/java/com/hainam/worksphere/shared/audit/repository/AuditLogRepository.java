package com.hainam.worksphere.shared.audit.repository;

import com.hainam.worksphere.shared.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    /**
     * Find audit logs by entity type and ID
     */
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    /**
     * Find audit logs within date range
     */
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find audit logs by multiple criteria
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByCriteria(@Param("userId") String userId,
                                  @Param("action") String action,
                                  @Param("entityType") String entityType,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);

    /**
     * Find failed audit logs
     */
    Page<AuditLog> findByStatusOrderByTimestampDesc(String status, Pageable pageable);

    /**
     * Count audit logs by user within date range
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.timestamp >= :startDate")
    Long countByUserIdAndTimestampAfter(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate);

    /**
     * Get audit statistics
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :startDate GROUP BY a.action")
    List<Object[]> getAuditStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * Find old audit logs for cleanup (batch processing)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp < :cutoffDate ORDER BY a.timestamp ASC")
    List<AuditLog> findTop1000ByTimestampBeforeOrderByTimestampAsc(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);
}
