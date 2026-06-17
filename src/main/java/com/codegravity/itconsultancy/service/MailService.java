package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.request.RegistrationEmailRequest;
import com.codegravity.itconsultancy.enums.EmailStatus;

public interface MailService {
    EmailStatus sendRegistrationEmail(
            String toEmail,
            String firstName,
            String lastName, // 👈 Added parameter here to ensure compilation matches implementation
            UserType userType,
            String generatedId,
            String degree,
            String major,
            String university,
            String workAuthorization,
            Boolean needsAccommodation,
            String tools
    );
}

    /**
     * Sends a registration confirmation email and persists a mail-log entry.
     * Build the request via {@link RegistrationEmailRequest#builder()}.
     */
    EmailStatus sendRegistrationEmail(RegistrationEmailRequest request);
}
