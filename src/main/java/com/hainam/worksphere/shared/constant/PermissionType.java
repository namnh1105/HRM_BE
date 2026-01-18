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
