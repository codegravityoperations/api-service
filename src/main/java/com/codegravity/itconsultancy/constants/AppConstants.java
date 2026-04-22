package com.codegravity.itconsultancy.constants;

/**
 * Application-wide constants.
 * All fields are public static final — no instances needed.
 *
 * NEVER instantiate this class. Constructor is private.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class — prevent instantiation
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // ─── ID Generation ──────────────────────────────────────────
    /** e.g. EMP-2024-0001 */
    public static final String EMPLOYEE_ID_PREFIX = "EMP";
    /** e.g. CAN-2024-0001 */
    public static final String CANDIDATE_ID_PREFIX = "CAN";
    /** Separator between prefix, year, and sequence number */
    public static final String ID_SEPARATOR = "-";
    /** Zero-padded sequence length: 0001 */
    public static final int ID_SEQUENCE_LENGTH = 4;

    // ─── Default Values ──────────────────────────────────────────
    /** Default status assigned to all new registrations */
    public static final String DEFAULT_STATUS = "ACTIVE";

    // ─── Date Formats ────────────────────────────────────────────
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // ─── Email ───────────────────────────────────────────────────
    public static final String REGISTRATION_EMAIL_SUBJECT = "Welcome to Code Gravity IT Consultancy — Registration Successful";
    public static final String EMAIL_TEMPLATE_EMPLOYEE = "emails/employee-registration";
    public static final String EMAIL_TEMPLATE_CANDIDATE = "emails/candidate-registration";

    // ─── Pagination Defaults ─────────────────────────────────────
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";
}