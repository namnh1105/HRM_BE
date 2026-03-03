package com.hainam.worksphere.employee.mapper;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeName", source = "store.name")
    @Mapping(target = "gender", expression = "java(employee.getGender() != null ? employee.getGender().name() : null)")
    @Mapping(target = "employmentStatus", expression = "java(employee.getEmploymentStatus() != null ? employee.getEmploymentStatus().name() : null)")
    EmployeeResponse toEmployeeResponse(Employee employee);
}
