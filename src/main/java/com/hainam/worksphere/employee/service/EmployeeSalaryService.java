package com.hainam.worksphere.employee.service;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.domain.EmployeeSalary;
import com.hainam.worksphere.employee.dto.request.CreateEmployeeSalaryRequest;
import com.hainam.worksphere.employee.dto.request.UpdateEmployeeSalaryRequest;
import com.hainam.worksphere.employee.dto.response.EmployeeSalaryResponse;
import com.hainam.worksphere.employee.mapper.EmployeeSalaryMapper;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.employee.repository.EmployeeSalaryRepository;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeSalaryService {

    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryMapper employeeSalaryMapper;

    public List<EmployeeSalaryResponse> getAllSalaries() {
        return employeeSalaryRepository.findAllActive()
                .stream()
                .map(employeeSalaryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public EmployeeSalaryResponse getSalaryById(UUID id) {
        EmployeeSalary salary = employeeSalaryRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Employee salary not found with id: " + id));
        return employeeSalaryMapper.toResponse(salary);
    }

    public List<EmployeeSalaryResponse> getSalariesByEmployeeId(UUID employeeId) {
        return employeeSalaryRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(employeeSalaryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public EmployeeSalaryResponse getCurrentSalaryByEmployeeId(UUID employeeId) {
        EmployeeSalary salary = employeeSalaryRepository.findCurrentByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("No current salary found for employee: " + employeeId));
        return employeeSalaryMapper.toResponse(salary);
    }

    @Transactional
    @AuditAction(type = ActionType.CREATE, entity = "EMPLOYEE_SALARY")
    public EmployeeSalaryResponse createSalary(CreateEmployeeSalaryRequest request, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        // Auto-close the previous open-ended salary record
        employeeSalaryRepository.findActiveOpenEndedByEmployeeId(request.getEmployeeId())
                .ifPresent(previousSalary -> {
                    LocalDate endDate = request.getEffectiveDate().minusDays(1);
                    previousSalary.setEndDate(endDate);
                    previousSalary.setUpdatedBy(createdBy);
                    employeeSalaryRepository.save(previousSalary);
                });

        EmployeeSalary salary = EmployeeSalary.builder()
                .employee(employee)
                .baseSalary(request.getBaseSalary())
                .effectiveDate(request.getEffectiveDate())
                .endDate(request.getEndDate())
                .createdBy(createdBy)
                .build();

        EmployeeSalary saved = employeeSalaryRepository.save(salary);
        AuditContext.registerCreated(saved);

        return employeeSalaryMapper.toResponse(saved);
    }

    @Transactional
    @AuditAction(type = ActionType.UPDATE, entity = "EMPLOYEE_SALARY")
    public EmployeeSalaryResponse updateSalary(UUID id, UpdateEmployeeSalaryRequest request, UUID updatedBy) {
        EmployeeSalary salary = employeeSalaryRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Employee salary not found with id: " + id));

        AuditContext.snapshot(salary);

        if (request.getBaseSalary() != null) {
            salary.setBaseSalary(request.getBaseSalary());
        }
        if (request.getEffectiveDate() != null) {
            salary.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getEndDate() != null) {
            salary.setEndDate(request.getEndDate());
        }

        salary.setUpdatedBy(updatedBy);
        EmployeeSalary saved = employeeSalaryRepository.save(salary);
        AuditContext.registerUpdated(saved);

        return employeeSalaryMapper.toResponse(saved);
    }

    @Transactional
    @AuditAction(type = ActionType.DELETE, entity = "EMPLOYEE_SALARY")
    public void deleteSalary(UUID id, UUID deletedBy) {
        EmployeeSalary salary = employeeSalaryRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Employee salary not found with id: " + id));

        AuditContext.registerDeleted(salary);

        salary.setIsDeleted(true);
        salary.setDeletedAt(LocalDateTime.now());
        salary.setDeletedBy(deletedBy);
        employeeSalaryRepository.save(salary);
    }
}
