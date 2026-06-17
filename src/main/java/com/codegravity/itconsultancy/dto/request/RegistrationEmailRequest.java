package com.codegravity.itconsultancy.dto.request;

import com.codegravity.itconsultancy.enums.UserType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Carries all data needed to render and send a registration confirmation email.
 * Use the builder — all document-upload flags default to false (safe for employees).
 */
@Getter
@Builder
public class RegistrationEmailRequest {

    private final String      toEmail;
    private final String      firstName;
    private final UserType    userType;
    private final String      generatedId;
    private final LocalDate   submissionDate;

    /** Candidate-specific upload flags — false by default (unused for employees). */
    @Builder.Default private final boolean resumeUploaded         = false;
    @Builder.Default private final boolean eadUploaded            = false;
    @Builder.Default private final boolean drivingLicenseUploaded = false;
}
