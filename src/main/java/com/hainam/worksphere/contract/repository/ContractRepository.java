package com.hainam.worksphere.contract.repository;

import com.hainam.worksphere.contract.domain.Contract;
import com.hainam.worksphere.contract.domain.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    @Query("SELECT c FROM Contract c WHERE c.isDeleted = false")
    List<Contract> findAllActive();

    @Query("SELECT c FROM Contract c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Contract> findActiveById(@Param("id") UUID id);

    @Query("SELECT c FROM Contract c WHERE c.employee.id = :employeeId AND c.isDeleted = false")
    List<Contract> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT c FROM Contract c WHERE c.employee.id = :employeeId AND c.status = :status AND c.isDeleted = false")
    List<Contract> findActiveByEmployeeIdAndStatus(@Param("employeeId") UUID employeeId, @Param("status") ContractStatus status);

    @Query("SELECT c FROM Contract c WHERE c.status = :status AND c.isDeleted = false")
    List<Contract> findActiveByStatus(@Param("status") ContractStatus status);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Contract c WHERE c.contractCode = :contractCode AND c.isDeleted = false")
    boolean existsActiveByContractCode(@Param("contractCode") String contractCode);
}
