package com.hainam.worksphere.employee.repository;

import com.hainam.worksphere.employee.domain.EmployeeSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalary, UUID> {

    @Query("SELECT es FROM EmployeeSalary es WHERE es.id = :id AND es.isDeleted = false")
    Optional<EmployeeSalary> findActiveById(@Param("id") UUID id);

    @Query("SELECT es FROM EmployeeSalary es WHERE es.employee.id = :employeeId AND es.isDeleted = false ORDER BY es.effectiveDate DESC")
    List<EmployeeSalary> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT es FROM EmployeeSalary es WHERE es.employee.id = :employeeId " +
            "AND es.isDeleted = false " +
            "AND es.effectiveDate <= CURRENT_DATE " +
            "AND (es.endDate IS NULL OR es.endDate >= CURRENT_DATE) " +
            "ORDER BY es.effectiveDate DESC")
    Optional<EmployeeSalary> findCurrentByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT es FROM EmployeeSalary es WHERE es.employee.id = :employeeId " +
            "AND es.isDeleted = false " +
            "AND es.endDate IS NULL " +
            "ORDER BY es.effectiveDate DESC")
    Optional<EmployeeSalary> findActiveOpenEndedByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT es FROM EmployeeSalary es WHERE es.isDeleted = false ORDER BY es.effectiveDate DESC")
    List<EmployeeSalary> findAllActive();
}

