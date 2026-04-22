package com.codegravity.itconsultancy.enums;

/**
 * Lifecycle status of a registered user account.
 *
 * PENDING  → Registered but not yet verified (future use)
 * ACTIVE   → Fully active account
 * INACTIVE → Soft-deleted or deactivated account
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    INACTIVE
}