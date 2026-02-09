package com.hainam.worksphere.contract.service;

import com.hainam.worksphere.contract.domain.Contract;
import com.hainam.worksphere.contract.domain.ContractStatus;
import com.hainam.worksphere.contract.dto.request.CreateContractRequest;
import com.hainam.worksphere.contract.dto.request.UpdateContractRequest;
import com.hainam.worksphere.contract.dto.response.ContractResponse;
import com.hainam.worksphere.contract.mapper.ContractMapper;
import com.hainam.worksphere.contract.repository.ContractRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.ContractNotFoundException;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractMapper contractMapper;

    @Transactional
    @CacheEvict(value = CacheConfig.CONTRACT_CACHE, allEntries = true)
    public ContractResponse createContract(CreateContractRequest request, UUID createdBy) {
        if (contractRepository.existsActiveByContractCode(request.getContractCode())) {
            throw new ValidationException("Contract code already exists: " + request.getContractCode());
        }

        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationException("End date must not be before start date");
        }

        Contract contract = Contract.builder()
                .contractCode(request.getContractCode())
                .employee(employee)
                .contractType(request.getContractType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .signingDate(request.getSigningDate())
                .baseSalary(request.getBaseSalary())
                .salaryCoefficient(request.getSalaryCoefficient() != null ? request.getSalaryCoefficient() : 1.0)
                .note(request.getNote())
                .attachmentUrl(request.getAttachmentUrl())
                .createdBy(createdBy)
                .build();

        Contract saved = contractRepository.save(contract);
        return contractMapper.toContractResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CONTRACT_CACHE, allEntries = true)
    public ContractResponse updateContract(UUID id, UpdateContractRequest request, UUID updatedBy) {
        Contract contract = contractRepository.findActiveById(id)
                .orElseThrow(() -> ContractNotFoundException.byId(id.toString()));

        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(contract.getStartDate())) {
                throw new ValidationException("End date must not be before start date");
            }
            contract.setEndDate(request.getEndDate());
        }
        if (request.getBaseSalary() != null) {
            contract.setBaseSalary(request.getBaseSalary());
        }
        if (request.getSalaryCoefficient() != null) {
            contract.setSalaryCoefficient(request.getSalaryCoefficient());
        }
        if (request.getStatus() != null) {
            contract.setStatus(request.getStatus());
        }
        if (request.getNote() != null) {
            contract.setNote(request.getNote());
        }
        if (request.getAttachmentUrl() != null) {
            contract.setAttachmentUrl(request.getAttachmentUrl());
        }
        contract.setUpdatedBy(updatedBy);

        Contract saved = contractRepository.save(contract);
        return contractMapper.toContractResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CONTRACT_CACHE, allEntries = true)
    public void deleteContract(UUID id, UUID deletedBy) {
        Contract contract = contractRepository.findActiveById(id)
                .orElseThrow(() -> ContractNotFoundException.byId(id.toString()));

        contract.setIsDeleted(true);
        contract.setDeletedAt(LocalDateTime.now());
        contract.setDeletedBy(deletedBy);
        contractRepository.save(contract);
    }

    @Cacheable(value = CacheConfig.CONTRACT_CACHE, key = "'all'")
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAllActive()
                .stream()
                .map(contractMapper::toContractResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.CONTRACT_CACHE, key = "#id.toString()")
    public ContractResponse getContractById(UUID id) {
        Contract contract = contractRepository.findActiveById(id)
                .orElseThrow(() -> ContractNotFoundException.byId(id.toString()));
        return contractMapper.toContractResponse(contract);
    }

    @Cacheable(value = CacheConfig.CONTRACT_CACHE, key = "'employee:' + #employeeId")
    public List<ContractResponse> getByEmployeeId(UUID employeeId) {
        return contractRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(contractMapper::toContractResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.CONTRACT_CACHE, key = "'active'")
    public List<ContractResponse> getActiveContracts() {
        return contractRepository.findActiveByStatus(ContractStatus.ACTIVE)
                .stream()
                .map(contractMapper::toContractResponse)
                .collect(Collectors.toList());
    }
}
