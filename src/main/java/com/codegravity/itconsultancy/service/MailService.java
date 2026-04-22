package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.UserType;

public interface MailService {
    EmailStatus sendRegistrationEmail(String toEmail, String firstName, UserType userType, String generatedId);
}