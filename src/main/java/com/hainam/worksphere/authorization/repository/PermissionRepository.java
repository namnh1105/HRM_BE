package com.hainam.worksphere.authorization.repository;

import com.hainam.worksphere.authorization.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    boolean existsByCode(String code);

    Optional<Permission> findByCode(String code);

    List<Permission> findByIsActiveTrue();

    List<Permission> findByIsSystemTrue();

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByResourceAndAction(String resource, String action);

    @Query("SELECT p FROM Permission p JOIN p.rolePermissions rp WHERE rp.role.id = :roleId AND rp.isActive = true AND p.isActive = true")
    List<Permission> findByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT p FROM Permission p JOIN p.rolePermissions rp WHERE rp.role.code = :roleCode AND rp.isActive = true AND p.isActive = true")
    List<Permission> findByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.rolePermissions rp " +
           "JOIN rp.role r " +
           "JOIN r.rolePermissions urp " +
           "JOIN UserRole ur ON ur.role.id = r.id " +
           "WHERE ur.userId = :userId AND ur.isActive = true AND rp.isActive = true AND p.isActive = true")
    List<Permission> findByUserId(@Param("userId") UUID userId);

    List<Permission> findByCodeIn(Set<String> codes);

    @Query("SELECT p FROM Permission p WHERE " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.displayName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "p.isActive = true")
    List<Permission> searchByCodeOrDisplayName(@Param("search") String search);
}
