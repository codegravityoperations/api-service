package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.request.RegistrationEmailRequest;
import com.codegravity.itconsultancy.enums.EmailStatus;

public interface MailService {

    /**
     * Sends a registration confirmation email and persists a mail-log entry.
     * Build the request via {@link RegistrationEmailRequest#builder()}.
     */
    EmailStatus sendRegistrationEmail(RegistrationEmailRequest request);
}
