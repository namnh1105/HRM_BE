package com.hainam.worksphere.user.mapper;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "givenName", source = "employee.firstName")
    @Mapping(target = "familyName", source = "employee.lastName")
    @Mapping(target = "avatarUrl", source = "employee.avatarUrl")
    @Mapping(target = "isActive", source = "isEnabled")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "isDeleted", source = "isDeleted")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "deletedBy", source = "deletedBy")
    UserResponse toUserResponse(User user, Employee employee);
}
