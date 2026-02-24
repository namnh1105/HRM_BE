package com.hainam.worksphere.workshift.service;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.exception.WorkShiftNotFoundException;
import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.dto.request.AssignWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.request.UpdateAssignWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.response.EmployeeWorkShiftResponse;
import com.hainam.worksphere.workshift.mapper.EmployeeWorkShiftMapper;
import com.hainam.worksphere.workshift.repository.EmployeeWorkShiftRepository;
import com.hainam.worksphere.workshift.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeWorkShiftService {

    private final EmployeeWorkShiftRepository employeeWorkShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeWorkShiftMapper employeeWorkShiftMapper;

    public List<EmployeeWorkShiftResponse> getByEmployeeId(UUID employeeId) {
        employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(employeeId.toString()));

        return employeeWorkShiftRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeWorkShiftResponse> getByEmployeeIdAndDate(UUID employeeId, LocalDate date) {
        employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(employeeId.toString()));

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return employeeWorkShiftRepository.findActiveByEmployeeIdAndDate(employeeId, date, dayOfWeek)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeWorkShiftResponse> getByWorkShiftId(UUID workShiftId) {
        workShiftRepository.findActiveById(workShiftId)
                .orElseThrow(() -> WorkShiftNotFoundException.byId(workShiftId.toString()));

        return employeeWorkShiftRepository.findActiveByWorkShiftId(workShiftId)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    public EmployeeWorkShiftResponse getById(UUID id) {
        EmployeeWorkShift ews = employeeWorkShiftRepository.findActiveById(id)
                .orElseThrow(() -> new ValidationException("Employee work shift assignment not found with id: " + id));
        return employeeWorkShiftMapper.toResponse(ews);
    }

    @Transactional
    public EmployeeWorkShiftResponse assignWorkShift(AssignWorkShiftRequest request, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        WorkShift workShift = workShiftRepository.findActiveById(request.getWorkShiftId())
                .orElseThrow(() -> WorkShiftNotFoundException.byId(request.getWorkShiftId().toString()));

        // Check for duplicate assignment on same day_of_week and date
        DayOfWeek checkDay = request.getDayOfWeek();
        if (checkDay != null) {
            boolean exists = employeeWorkShiftRepository.existsActiveAssignment(
                    request.getEmployeeId(), request.getWorkShiftId(),
                    request.getDate(), checkDay
            );
            if (exists) {
                throw new ValidationException(
                        "Employee already has this work shift assigned for " + checkDay +
                        " on the date"
                );
            }
        }

        EmployeeWorkShift ews = employeeWorkShiftMapper.toEntity(request);
        ews.setEmployee(employee);
        ews.setWorkShift(workShift);
        ews.setCreatedBy(createdBy);

        EmployeeWorkShift saved = employeeWorkShiftRepository.save(ews);
        return employeeWorkShiftMapper.toResponse(saved);
    }

    @Transactional
    public EmployeeWorkShiftResponse updateAssignment(UUID id, UpdateAssignWorkShiftRequest request, UUID updatedBy) {
        EmployeeWorkShift ews = employeeWorkShiftRepository.findActiveById(id)
                .orElseThrow(() -> new ValidationException("Employee work shift assignment not found with id: " + id));

        if (request.getDate() != null) {
            ews.setDate(request.getDate());
        }
        if (request.getDayOfWeek() != null) {
            ews.setDayOfWeek(request.getDayOfWeek());
        }

        ews.setUpdatedBy(updatedBy);
        EmployeeWorkShift saved = employeeWorkShiftRepository.save(ews);
        return employeeWorkShiftMapper.toResponse(saved);
    }

    @Transactional
    public void softDelete(UUID id, UUID deletedBy) {
        EmployeeWorkShift ews = employeeWorkShiftRepository.findActiveById(id)
                .orElseThrow(() -> new ValidationException("Employee work shift assignment not found with id: " + id));

        ews.setIsDeleted(true);
        ews.setDeletedAt(LocalDateTime.now());
        ews.setDeletedBy(deletedBy);
        employeeWorkShiftRepository.save(ews);
    }

    public List<EmployeeWorkShiftResponse> getByUserIdAndDate(UUID userId, LocalDate date) {
        Employee employee = employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byId("user:" + userId));

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return employeeWorkShiftRepository.findActiveByEmployeeIdAndDate(employee.getId(), date, dayOfWeek)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeWorkShiftResponse> getAllByUserId(UUID userId) {
        Employee employee = employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byId("user:" + userId));

        return employeeWorkShiftRepository.findActiveByEmployeeId(employee.getId())
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }
}
