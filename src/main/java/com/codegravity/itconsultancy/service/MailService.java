package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.UserType;

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