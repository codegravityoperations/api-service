package com.codegravity.itconsultancy.repository;

import com.codegravity.itconsultancy.entity.MailLog;
import com.codegravity.itconsultancy.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailLogRepository extends JpaRepository<MailLog, Long> {

    List<MailLog> findByRecipient(String recipient);

    List<MailLog> findByStatus(EmailStatus status);
}