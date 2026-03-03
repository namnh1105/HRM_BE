package com.hainam.worksphere.workshift.repository;

import com.hainam.worksphere.workshift.domain.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, UUID> {

    @Query("SELECT ws FROM WorkShift ws WHERE ws.isDeleted = false")
    List<WorkShift> findAllActive();

    @Query("SELECT ws FROM WorkShift ws WHERE ws.id = :id AND ws.isDeleted = false")
    Optional<WorkShift> findActiveById(@Param("id") UUID id);

    @Query("SELECT ws FROM WorkShift ws WHERE ws.code = :code AND ws.isDeleted = false")
    Optional<WorkShift> findActiveByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(ws) > 0 THEN true ELSE false END FROM WorkShift ws WHERE ws.code = :code AND ws.isDeleted = false")
    boolean existsActiveByCode(@Param("code") String code);

    @Query("SELECT ws FROM WorkShift ws WHERE ws.isActive = true AND ws.isDeleted = false")
    List<WorkShift> findAllActiveAndEnabled();

    @Query("SELECT ws FROM WorkShift ws WHERE ws.store.id = :storeId AND ws.isDeleted = false")
    List<WorkShift> findActiveByStoreId(@Param("storeId") UUID storeId);
}
