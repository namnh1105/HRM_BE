package com.hainam.worksphere.payroll.service;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.domain.EmployeeSalary;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.employee.repository.EmployeeSalaryRepository;
import com.hainam.worksphere.payroll.domain.Payroll;
import com.hainam.worksphere.payroll.domain.PayrollStatus;
import com.hainam.worksphere.payroll.dto.request.CreatePayrollRequest;
import com.hainam.worksphere.payroll.dto.request.UpdatePayrollRequest;
import com.hainam.worksphere.payroll.dto.response.PayrollResponse;
import com.hainam.worksphere.payroll.mapper.PayrollMapper;
import com.hainam.worksphere.payroll.repository.PayrollRepository;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.PayrollNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollMapper payrollMapper;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;

    @Cacheable(value = CacheConfig.PAYROLL_CACHE, key = "'all'")
    public List<PayrollResponse> getAllPayrolls() {
        return payrollRepository.findAllActive()
                .stream()
                .map(payrollMapper::toPayrollResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.PAYROLL_CACHE, key = "'id:' + #id")
    public PayrollResponse getPayrollById(UUID id) {
        Payroll payroll = payrollRepository.findActiveById(id)
                .orElseThrow(() -> PayrollNotFoundException.byId(id.toString()));
        return payrollMapper.toPayrollResponse(payroll);
    }

    @Cacheable(value = CacheConfig.PAYROLL_CACHE, key = "'employee:' + #employeeId")
    public List<PayrollResponse> getByEmployeeId(UUID employeeId) {
        return payrollRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(payrollMapper::toPayrollResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.PAYROLL_CACHE, key = "'month:' + #month + ':year:' + #year")
    public List<PayrollResponse> getByMonthAndYear(Integer month, Integer year) {
        return payrollRepository.findActiveByMonthAndYear(month, year)
                .stream()
                .map(payrollMapper::toPayrollResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "PAYROLL")
    public PayrollResponse createPayroll(CreatePayrollRequest request, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        payrollRepository.findActiveByEmployeeIdAndMonthAndYear(
                request.getEmployeeId(), request.getMonth(), request.getYear()
        ).ifPresent(existing -> {
            throw new ValidationException("Payroll already exists for this employee in " + request.getMonth() + "/" + request.getYear());
        });

        // Resolve baseSalary: use provided value, or look up from employee_salaries
        Double baseSalary = request.getBaseSalary();
        if (baseSalary == null) {
            baseSalary = employeeSalaryRepository.findCurrentByEmployeeId(request.getEmployeeId())
                    .map(EmployeeSalary::getBaseSalary)
                    .orElseThrow(() -> new ValidationException("No current salary found for employee. Please provide base_salary or create an employee salary record."));
        }

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(request.getMonth())
                .year(request.getYear())
                .baseSalary(baseSalary)
                .salaryCoefficient(request.getSalaryCoefficient() != null ? request.getSalaryCoefficient() : 1.0)
                .workingDays(request.getWorkingDays())
                .actualWorkingDays(request.getActualWorkingDays())
                .overtimeHours(request.getOvertimeHours() != null ? request.getOvertimeHours() : 0.0)
                .overtimePay(request.getOvertimePay() != null ? request.getOvertimePay() : 0.0)
                .allowance(request.getAllowance() != null ? request.getAllowance() : 0.0)
                .bonus(request.getBonus() != null ? request.getBonus() : 0.0)
                .socialInsurance(request.getSocialInsurance() != null ? request.getSocialInsurance() : 0.0)
                .healthInsurance(request.getHealthInsurance() != null ? request.getHealthInsurance() : 0.0)
                .unemploymentInsurance(request.getUnemploymentInsurance() != null ? request.getUnemploymentInsurance() : 0.0)
                .personalIncomeTax(request.getPersonalIncomeTax() != null ? request.getPersonalIncomeTax() : 0.0)
                .note(request.getNote())
                .status(PayrollStatus.DRAFT)
                .createdBy(createdBy)
                .build();

        calculatePayroll(payroll);

        Payroll saved = payrollRepository.save(payroll);
        AuditContext.registerCreated(saved);
        return payrollMapper.toPayrollResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "PAYROLL")
    public PayrollResponse updatePayroll(UUID id, UpdatePayrollRequest request, UUID updatedBy) {
        Payroll payroll = payrollRepository.findActiveById(id)
                .orElseThrow(() -> PayrollNotFoundException.byId(id.toString()));

        AuditContext.snapshot(payroll);

        if (request.getBaseSalary() != null) {
            payroll.setBaseSalary(request.getBaseSalary());
        }
        if (request.getSalaryCoefficient() != null) {
            payroll.setSalaryCoefficient(request.getSalaryCoefficient());
        }
        if (request.getWorkingDays() != null) {
            payroll.setWorkingDays(request.getWorkingDays());
        }
        if (request.getActualWorkingDays() != null) {
            payroll.setActualWorkingDays(request.getActualWorkingDays());
        }
        if (request.getOvertimeHours() != null) {
            payroll.setOvertimeHours(request.getOvertimeHours());
        }
        if (request.getOvertimePay() != null) {
            payroll.setOvertimePay(request.getOvertimePay());
        }
        if (request.getAllowance() != null) {
            payroll.setAllowance(request.getAllowance());
        }
        if (request.getBonus() != null) {
            payroll.setBonus(request.getBonus());
        }
        if (request.getSocialInsurance() != null) {
            payroll.setSocialInsurance(request.getSocialInsurance());
        }
        if (request.getHealthInsurance() != null) {
            payroll.setHealthInsurance(request.getHealthInsurance());
        }
        if (request.getUnemploymentInsurance() != null) {
            payroll.setUnemploymentInsurance(request.getUnemploymentInsurance());
        }
        if (request.getPersonalIncomeTax() != null) {
            payroll.setPersonalIncomeTax(request.getPersonalIncomeTax());
        }
        if (request.getStatus() != null) {
            payroll.setStatus(request.getStatus());
        }
        if (request.getPaymentDate() != null) {
            payroll.setPaymentDate(request.getPaymentDate());
        }
        if (request.getNote() != null) {
            payroll.setNote(request.getNote());
        }

        payroll.setUpdatedBy(updatedBy);
        calculatePayroll(payroll);

        Payroll saved = payrollRepository.save(payroll);
        AuditContext.registerUpdated(saved);
        return payrollMapper.toPayrollResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "PAYROLL")
    public void deletePayroll(UUID id, UUID deletedBy) {
        Payroll payroll = payrollRepository.findActiveById(id)
                .orElseThrow(() -> PayrollNotFoundException.byId(id.toString()));

        AuditContext.registerDeleted(payroll);

        payroll.setIsDeleted(true);
        payroll.setDeletedAt(Instant.now());
        payroll.setDeletedBy(deletedBy);
        payrollRepository.save(payroll);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "PAYROLL", actionCode = "CONFIRM_PAYROLL")
    public PayrollResponse confirmPayroll(UUID id, UUID updatedBy) {
        Payroll payroll = payrollRepository.findActiveById(id)
                .orElseThrow(() -> PayrollNotFoundException.byId(id.toString()));

        AuditContext.snapshot(payroll);

        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new ValidationException("Only DRAFT payrolls can be confirmed");
        }

        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setUpdatedBy(updatedBy);

        Payroll saved = payrollRepository.save(payroll);
        AuditContext.registerUpdated(saved);
        return payrollMapper.toPayrollResponse(saved);
    }

    /**
     * Auto-calculate totalDeductions, totalIncome, and netSalary on the entity.
     */
    private void calculatePayroll(Payroll payroll) {
        double baseSalary = payroll.getBaseSalary() != null ? payroll.getBaseSalary() : 0.0;
        double salaryCoefficient = payroll.getSalaryCoefficient() != null ? payroll.getSalaryCoefficient() : 1.0;
        double overtimePay = payroll.getOvertimePay() != null ? payroll.getOvertimePay() : 0.0;
        double allowance = payroll.getAllowance() != null ? payroll.getAllowance() : 0.0;
        double bonus = payroll.getBonus() != null ? payroll.getBonus() : 0.0;

        int workingDays = payroll.getWorkingDays() != null && payroll.getWorkingDays() > 0 ? payroll.getWorkingDays() : 1;
        int actualWorkingDays = payroll.getActualWorkingDays() != null ? payroll.getActualWorkingDays() : workingDays;

        double proratedSalary = (baseSalary * salaryCoefficient / workingDays) * actualWorkingDays;
        double totalIncome = proratedSalary + overtimePay + allowance + bonus;

        double socialInsurance = payroll.getSocialInsurance() != null ? payroll.getSocialInsurance() : 0.0;
        double healthInsurance = payroll.getHealthInsurance() != null ? payroll.getHealthInsurance() : 0.0;
        double unemploymentInsurance = payroll.getUnemploymentInsurance() != null ? payroll.getUnemploymentInsurance() : 0.0;
        double personalIncomeTax = payroll.getPersonalIncomeTax() != null ? payroll.getPersonalIncomeTax() : 0.0;
        double totalDeductions = socialInsurance + healthInsurance + unemploymentInsurance + personalIncomeTax;

        double netSalary = totalIncome - totalDeductions;

        payroll.setTotalIncome(totalIncome);
        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetSalary(netSalary);
    }
}
