package com.hainam.worksphere.employee.mapper;

import com.hainam.worksphere.employee.domain.EmployeeSalary;
import com.hainam.worksphere.employee.dto.response.EmployeeSalaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeSalaryMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    EmployeeSalaryResponse toResponse(EmployeeSalary employeeSalary);
}

