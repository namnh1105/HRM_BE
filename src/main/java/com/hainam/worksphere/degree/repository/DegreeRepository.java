package com.hainam.worksphere.degree.repository;

import com.hainam.worksphere.degree.domain.Degree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DegreeRepository extends JpaRepository<Degree, UUID> {

    @Query("SELECT d FROM Degree d WHERE d.isDeleted = false")
    List<Degree> findAllActive();

    @Query("SELECT d FROM Degree d WHERE d.id = :id AND d.isDeleted = false")
    Optional<Degree> findActiveById(@Param("id") UUID id);

    @Query("SELECT d FROM Degree d WHERE d.employee.id = :employeeId AND d.isDeleted = false")
    List<Degree> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);
}
