package com.codegravity.itconsultancy.enums;

/**
 * Lifecycle status of a registered user account.
 *
 * PENDING  → Registered but not yet verified (future use)
 * ACTIVE   → Fully active account
 * INACTIVE → Deactivated account
 * DELETED  → Soft-deleted account
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    DELETED
}