package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.constants.AppConstants;
import com.codegravity.itconsultancy.entity.MailLog;
import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.UserType;
import com.codegravity.itconsultancy.repository.MailLogRepository;
import com.codegravity.itconsultancy.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailLogRepository mailLogRepository;

    @Override
    public EmailStatus sendRegistrationEmail(String toEmail,
                                             String firstName,
                                             String lastName, // 👈 Added parameter here
                                             UserType userType,
                                             String generatedId,
                                             String degree,
                                             String major,
                                             String university,
                                             String workAuthorization,
                                             Boolean needsAccommodation,
                                             String tools) {

        String template = userType == UserType.EMPLOYEE
                ? AppConstants.EMAIL_TEMPLATE_EMPLOYEE
                : AppConstants.EMAIL_TEMPLATE_CANDIDATE;

        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName); // 🚀 Injected here so Thymeleaf can render it
        context.setVariable("generatedId", generatedId);
        context.setVariable("userType", userType.name());
        context.setVariable("degree", degree);
        context.setVariable("major", major);
        context.setVariable("university", university);
        context.setVariable("workAuthorization", workAuthorization);
        context.setVariable("needsAccommodation", needsAccommodation);
        context.setVariable("email", toEmail);
        context.setVariable("toolsInterested", tools);

        String htmlBody = templateEngine.process(template, context);

        EmailStatus status;
        String errorMsg = null;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(AppConstants.REGISTRATION_EMAIL_SUBJECT);
            helper.setText(htmlBody, true);
            mailSender.send(message);

            status = EmailStatus.SENT;
            log.info("Registration email sent to {} | id: {}", toEmail, generatedId);

        } catch (MessagingException e) {
            status = EmailStatus.FAILED;
            errorMsg = e.getMessage();
            log.error("Failed to send registration email to {} | reason: {}", toEmail, errorMsg);
        }

        MailLog mailLog = MailLog.builder()
                .recipient(toEmail)
                .subject(AppConstants.REGISTRATION_EMAIL_SUBJECT)
                .body(htmlBody)
                .userType(userType)
                .referenceId(generatedId)
                .status(status)
                .sentAt(status == EmailStatus.SENT ? LocalDateTime.now() : null)
                .errorMsg(errorMsg)
                .build();

        mailLogRepository.save(mailLog);

        return status;
    }
}