package com.hainam.worksphere.shared.constant;

public enum PermissionType {
    // User profile permissions
    VIEW_PROFILE("View user profile"),
    UPDATE_PROFILE("Update user profile"),
    DELETE_PROFILE("Delete user profile"),

    // User management permissions
    MANAGE_USER("Manage users"),
    VIEW_USER("View users"),
    CREATE_USER("Create users"),
    UPDATE_USER("Update users"),
    DELETE_USER("Delete users"),

    // Soft delete specific permissions
    RESTORE_USER("Restore deleted users"),
    PERMANENT_DELETE_USER("Permanently delete users"),

    // Department management permissions
    VIEW_DEPARTMENT("View departments"),
    CREATE_DEPARTMENT("Create departments"),
    UPDATE_DEPARTMENT("Update departments"),
    DELETE_DEPARTMENT("Delete departments"),

    // Employee management permissions
    VIEW_EMPLOYEE("View employees"),
    CREATE_EMPLOYEE("Create employees"),
    UPDATE_EMPLOYEE("Update employees"),
    DELETE_EMPLOYEE("Delete employees"),

    // Employee salary management permissions
    VIEW_EMPLOYEE_SALARY("View employee salaries"),
    CREATE_EMPLOYEE_SALARY("Create employee salaries"),
    UPDATE_EMPLOYEE_SALARY("Update employee salaries"),
    DELETE_EMPLOYEE_SALARY("Delete employee salaries"),

    // Attendance management permissions
    VIEW_ATTENDANCE("View attendance records"),
    CREATE_ATTENDANCE("Create attendance records"),
    UPDATE_ATTENDANCE("Update attendance records"),
    DELETE_ATTENDANCE("Delete attendance records"),

    // Work shift management permissions
    VIEW_WORK_SHIFT("View work shifts"),
    CREATE_WORK_SHIFT("Create work shifts"),
    UPDATE_WORK_SHIFT("Update work shifts"),
    DELETE_WORK_SHIFT("Delete work shifts"),

    // Role and permission management
    MANAGE_ROLES("Manage roles"),
    MANAGE_PERMISSIONS("Manage permissions"),
    ASSIGN_ROLES("Assign roles to users"),
    REVOKE_ROLES("Revoke roles from users"),

    // User role management
    MANAGE_USER_ROLE("Manage user role assignments"),
    READ_USER_ROLE("Read user role assignments"),

    // System administration
    SYSTEM_ADMIN("System administration privileges"),
    SUPER_ADMIN("Super administrator privileges"),

    // Leave request permissions
    VIEW_LEAVE_REQUEST("View leave requests"),
    CREATE_LEAVE_REQUEST("Create leave requests"),
    APPROVE_LEAVE_REQUEST("Approve leave requests"),
    DELETE_LEAVE_REQUEST("Delete leave requests"),

    // Contract management permissions
    VIEW_CONTRACT("View contracts"),
    CREATE_CONTRACT("Create contracts"),
    UPDATE_CONTRACT("Update contracts"),
    DELETE_CONTRACT("Delete contracts"),

    // Payroll management permissions
    VIEW_PAYROLL("View payrolls"),
    CREATE_PAYROLL("Create payrolls"),
    UPDATE_PAYROLL("Update payrolls"),
    DELETE_PAYROLL("Delete payrolls"),

    // Insurance management permissions
    VIEW_INSURANCE("View insurances"),
    CREATE_INSURANCE("Create insurances"),
    UPDATE_INSURANCE("Update insurances"),
    DELETE_INSURANCE("Delete insurances"),

    // Degree management permissions
    VIEW_DEGREE("View degrees"),
    CREATE_DEGREE("Create degrees"),
    UPDATE_DEGREE("Update degrees"),
    DELETE_DEGREE("Delete degrees"),

    // Relative management permissions
    VIEW_RELATIVE("View relatives"),
    CREATE_RELATIVE("Create relatives"),
    UPDATE_RELATIVE("Update relatives"),
    DELETE_RELATIVE("Delete relatives"),

    // Audit logging permissions
    VIEW_AUDIT_LOGS("View audit logs"),
    MANAGE_AUDIT_LOGS("Manage audit logs");

    private final String description;

    PermissionType(String description) {
        this.description = description;
    }

    public String key() {
        return name();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name();
    }
}
