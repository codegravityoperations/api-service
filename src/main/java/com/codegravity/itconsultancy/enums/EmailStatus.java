package com.codegravity.itconsultancy.enums;

/**
 * Tracks the delivery status of registration confirmation emails.
 * Stored in the mail_log table for audit and retry purposes.
 */
public enum EmailStatus {
    PENDING,  // Email queued but not yet sent
    SENT,     // Successfully delivered to mail server
    FAILED    // Delivery failed — reason stored in mail_log.failure_reason
}