-- ============================================================
-- V1 | Initial Schema
-- Author  : IT Consultancy Team
-- Created : 2026
-- Purpose : Baseline schema — tables created manually before
--           Flyway was introduced. Flyway skips this version
--           due to baseline-version=1 in application.properties.
--           This file serves as DOCUMENTATION of the initial state.
-- ============================================================

-- Roles lookup table
CREATE TABLE IF NOT EXISTS `roles` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)   NOT NULL,
  `description` VARCHAR(255),
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_role_name` (`name`)
);

-- Mail audit log
CREATE TABLE IF NOT EXISTS `mail_logs` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `recipient`   VARCHAR(255)  NOT NULL,
  `subject`     VARCHAR(500)  NOT NULL,
  `body`        TEXT,
  `user_type`   VARCHAR(20)   NOT NULL,
  `reference_id`VARCHAR(20),
  `status`      VARCHAR(10)   DEFAULT 'SENT',
  `sent_at`     DATETIME,
  `error_msg`   TEXT,
  PRIMARY KEY (`id`),
  INDEX `idx_mail_recipient` (`recipient`),
  INDEX `idx_mail_status`    (`status`),
  INDEX `idx_mail_user_type` (`user_type`),
  CONSTRAINT `chk_mail_status`    CHECK (`status`    IN ('SENT','FAILED')),
  CONSTRAINT `chk_mail_user_type` CHECK (`user_type` IN ('EMPLOYEE','CANDIDATE','ADMIN'))
);

-- Candidates
CREATE TABLE IF NOT EXISTS `candidates` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `candidate_id` VARCHAR(20)   NOT NULL,
  `first_name`   VARCHAR(100)  NOT NULL,
  `last_name`    VARCHAR(100)  NOT NULL,
  `email`        VARCHAR(255)  NOT NULL,
  `password`     VARCHAR(255)  NOT NULL,
  `phone`        VARCHAR(20)   NOT NULL,
  `address`      VARCHAR(255),
  `applied_role` VARCHAR(150),
  `resume_url`   VARCHAR(500),
  `notes`        TEXT,
  `status`       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
  `is_active`    TINYINT(1)    NOT NULL DEFAULT 1,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_candidate_id`    (`candidate_id`),
  UNIQUE KEY `uq_candidate_email` (`email`),
  INDEX `idx_candidate_email`     (`email`),
  CONSTRAINT `chk_candidate_status` CHECK (`status` IN ('ACTIVE','INACTIVE','PENDING','TERMINATED'))
);

-- Employees
CREATE TABLE IF NOT EXISTS `employees` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `employee_id` VARCHAR(20)   NOT NULL,
  `first_name`  VARCHAR(100)  NOT NULL,
  `last_name`   VARCHAR(100)  NOT NULL,
  `email`       VARCHAR(255)  NOT NULL,
  `password`    VARCHAR(255)  NOT NULL,
  `phone`       VARCHAR(20)   NOT NULL,
  `address`     VARCHAR(255),
  `is_active`   TINYINT(1)    NOT NULL DEFAULT 1,
  `status`      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_employee_id`    (`employee_id`),
  UNIQUE KEY `uq_employee_email` (`email`),
  INDEX `idx_employee_email`     (`email`),
  CONSTRAINT `chk_employee_status` CHECK (`status` IN ('ACTIVE','INACTIVE','PENDING','TERMINATED'))
);