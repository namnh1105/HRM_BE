package com.hainam.worksphere.degree.service;

import com.hainam.worksphere.degree.domain.Degree;
import com.hainam.worksphere.degree.dto.request.CreateDegreeRequest;
import com.hainam.worksphere.degree.dto.response.DegreeResponse;
import com.hainam.worksphere.degree.mapper.DegreeMapper;
import com.hainam.worksphere.degree.repository.DegreeRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.DegreeNotFoundException;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
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
public class DegreeService {

    private final DegreeRepository degreeRepository;
    private final EmployeeRepository employeeRepository;
    private final DegreeMapper degreeMapper;

    @Transactional
    @CacheEvict(value = CacheConfig.DEGREE_CACHE, allEntries = true)
    public DegreeResponse createDegree(CreateDegreeRequest request, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        Degree degree = Degree.builder()
                .employee(employee)
                .degreeName(request.getDegreeName())
                .degreeLevel(request.getDegreeLevel())
                .major(request.getMajor())
                .institution(request.getInstitution())
                .graduationDate(request.getGraduationDate())
                .gpa(request.getGpa())
                .attachmentUrl(request.getAttachmentUrl())
                .note(request.getNote())
                .createdBy(createdBy)
                .build();

        Degree saved = degreeRepository.save(degree);
        return degreeMapper.toDegreeResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.DEGREE_CACHE, allEntries = true)
    public DegreeResponse updateDegree(UUID id, CreateDegreeRequest request, UUID updatedBy) {
        Degree degree = degreeRepository.findActiveById(id)
                .orElseThrow(() -> DegreeNotFoundException.byId(id.toString()));

        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                    .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));
            degree.setEmployee(employee);
        }
        if (request.getDegreeName() != null) {
            degree.setDegreeName(request.getDegreeName());
        }
        if (request.getDegreeLevel() != null) {
            degree.setDegreeLevel(request.getDegreeLevel());
        }
        if (request.getMajor() != null) {
            degree.setMajor(request.getMajor());
        }
        if (request.getInstitution() != null) {
            degree.setInstitution(request.getInstitution());
        }
        if (request.getGraduationDate() != null) {
            degree.setGraduationDate(request.getGraduationDate());
        }
        if (request.getGpa() != null) {
            degree.setGpa(request.getGpa());
        }
        if (request.getAttachmentUrl() != null) {
            degree.setAttachmentUrl(request.getAttachmentUrl());
        }
        if (request.getNote() != null) {
            degree.setNote(request.getNote());
        }
        degree.setUpdatedBy(updatedBy);

        Degree saved = degreeRepository.save(degree);
        return degreeMapper.toDegreeResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.DEGREE_CACHE, allEntries = true)
    public void deleteDegree(UUID id, UUID deletedBy) {
        Degree degree = degreeRepository.findActiveById(id)
                .orElseThrow(() -> DegreeNotFoundException.byId(id.toString()));

        degree.setIsDeleted(true);
        degree.setDeletedAt(LocalDateTime.now());
        degree.setDeletedBy(deletedBy);
        degreeRepository.save(degree);
    }

    @Cacheable(value = CacheConfig.DEGREE_CACHE, key = "'all'")
    public List<DegreeResponse> getAllDegrees() {
        return degreeRepository.findAllActive()
                .stream()
                .map(degreeMapper::toDegreeResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.DEGREE_CACHE, key = "#id.toString()")
    public DegreeResponse getDegreeById(UUID id) {
        Degree degree = degreeRepository.findActiveById(id)
                .orElseThrow(() -> DegreeNotFoundException.byId(id.toString()));
        return degreeMapper.toDegreeResponse(degree);
    }

    @Cacheable(value = CacheConfig.DEGREE_CACHE, key = "'employee:' + #employeeId.toString()")
    public List<DegreeResponse> getByEmployeeId(UUID employeeId) {
        return degreeRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(degreeMapper::toDegreeResponse)
                .collect(Collectors.toList());
    }
}
