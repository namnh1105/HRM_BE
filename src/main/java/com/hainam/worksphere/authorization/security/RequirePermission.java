package com.hainam.worksphere.authorization.security;

import com.hainam.worksphere.shared.constant.PermissionType;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(null, #permission.key())")
public @interface RequirePermission {
    PermissionType value();
}
