package com.codegravity.itconsultancy.constants;

/**
 * Security-related constants for JWT handling.
 *
 * NOTE: Actual secret values are injected from application.properties
 * via @Value in JwtTokenProvider. Only non-sensitive structural
 * constants live here.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // ─── JWT ─────────────────────────────────────────────────────
    /** HTTP header where JWT is expected */
    public static final String AUTH_HEADER = "Authorization";

    /** Prefix before the token in the Authorization header */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** JWT claim key for the user's role */
    public static final String CLAIM_ROLE = "role";

    /** JWT claim key for the user type (EMPLOYEE / CANDIDATE) */
    public static final String CLAIM_USER_TYPE = "userType";

    /** JWT claim key for the generated business ID */
    public static final String CLAIM_BUSINESS_ID = "businessId";

    // ─── BCrypt ──────────────────────────────────────────────────
    /**
     * BCrypt cost factor (work factor).
     * Higher = more secure but slower.
     * 12 is the enterprise standard balance point.
     * (10 = default, 14+ = very slow)
     */
    public static final int BCRYPT_STRENGTH = 12;
}