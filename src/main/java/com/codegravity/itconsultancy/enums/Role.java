package com.codegravity.itconsultancy.enums;

/**
 * Spring Security roles for role-based access control (RBAC).
 *
 * Convention: Spring Security requires roles to be prefixed with "ROLE_"
 * when using hasRole(). We store the full name so we can use it directly.
 *
 * Usage: @PreAuthorize("hasRole('EMPLOYEE')")
 */
public enum Role {
    ROLE_EMPLOYEE,
    ROLE_CANDIDATE,
    ROLE_ADMIN;

    /**
     * Returns the role name without the ROLE_ prefix.
     * Useful for display and token claims.
     * e.g. ROLE_EMPLOYEE → "EMPLOYEE"
     */
    public String getSimpleName() {
        return this.name().replace("ROLE_", "");
    }
}