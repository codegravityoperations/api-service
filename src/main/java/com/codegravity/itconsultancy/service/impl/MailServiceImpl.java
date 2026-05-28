package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.constants.AppConstants;
import com.codegravity.itconsultancy.dto.request.RegistrationEmailRequest;
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
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private final JavaMailSender    mailSender;
    private final TemplateEngine    templateEngine;
    private final MailLogRepository mailLogRepository;

    @Override
    public EmailStatus sendRegistrationEmail(RegistrationEmailRequest req) {

        String template = req.getUserType() == UserType.EMPLOYEE
                ? AppConstants.EMAIL_TEMPLATE_EMPLOYEE
                : AppConstants.EMAIL_TEMPLATE_CANDIDATE;

        // ── Build Thymeleaf context ──────────────────────────────
        Context ctx = new Context();
        ctx.setVariable("firstName",    req.getFirstName());
        ctx.setVariable("generatedId",  req.getGeneratedId());
        ctx.setVariable("userType",     req.getUserType().name());

        // Submission date — format once here, keep template logic-free
        String formattedDate = req.getSubmissionDate() != null
                ? req.getSubmissionDate().format(DATE_FMT)
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        ctx.setVariable("submissionDate", formattedDate);

        // Document upload statuses (candidate only; ignored by employee template)
        ctx.setVariable("resumeUploaded",         req.isResumeUploaded());
        ctx.setVariable("eadUploaded",            req.isEadUploaded());
        ctx.setVariable("drivingLicenseUploaded", req.isDrivingLicenseUploaded());

        String htmlBody = templateEngine.process(template, ctx);

        // ── Send ─────────────────────────────────────────────────
        EmailStatus status;
        String errorMsg = null;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(req.getToEmail());
            helper.setSubject(AppConstants.REGISTRATION_EMAIL_SUBJECT);
            helper.setText(htmlBody, true);
            mailSender.send(message);

            status = EmailStatus.SENT;
            log.info("Registration email sent to {} | id: {}", req.getToEmail(), req.getGeneratedId());

        } catch (MessagingException e) {
            status = EmailStatus.FAILED;
            errorMsg = e.getMessage();
            log.error("Failed to send registration email to {} | reason: {}", req.getToEmail(), errorMsg);
        }

        // ── Always log ────────────────────────────────────────────
        mailLogRepository.save(MailLog.builder()
                .recipient(req.getToEmail())
                .subject(AppConstants.REGISTRATION_EMAIL_SUBJECT)
                .body(htmlBody)
                .userType(req.getUserType())
                .referenceId(req.getGeneratedId())
                .status(status)
                .sentAt(status == EmailStatus.SENT ? LocalDateTime.now() : null)
                .errorMsg(errorMsg)
                .build());

        return status;
    }
}
