package com.codegravity.itconsultancy.constants;

/**
 * All REST API endpoint paths as constants.
 *
 * Why? If you change "/api/employees" to "/api/v1/employees",
 * you change it HERE once — not in 10 different controller methods.
 *
 * Pattern: BASE → RESOURCE → ACTION
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
    // ─── Public endpoints (no JWT required) ──────────────────────
    // Used in SecurityConfig to whitelist these from authentication
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/employees/register",
            "/api/candidates/register",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/error"
    };
}