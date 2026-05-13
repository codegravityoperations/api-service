-- ============================================================
-- V2 | JWT Refresh Tokens + RBAC + Constraint Fixes
-- Author  : IT Consultancy Team
-- Purpose : Adds infrastructure needed for:
--           1. JWT refresh token storage and revocation
--           2. Role-based access control (employee_roles / candidate_roles)
--           3. Default role seed data
--           4. Fix mail_logs CHECK constraint to allow PENDING status
-- ============================================================


-- ────────────────────────────────────────────────────────────
-- 1. REFRESH TOKEN TABLE
--    Stores refresh tokens server-side so we can revoke them.
--    cannot support logout or token revocation.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `refresh_tokens` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `token`       VARCHAR(500)  NOT NULL,           -- The actual refresh token string
  `user_email`  VARCHAR(255)  NOT NULL,           -- Who owns this token
  `user_type`   VARCHAR(20)   NOT NULL,           -- EMPLOYEE | CANDIDATE | ADMIN
  `expiry_date` DATETIME      NOT NULL,           -- When this token expires
  `revoked`     TINYINT(1)    NOT NULL DEFAULT 0, -- 1 = logged out / invalidated
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY  `uq_refresh_token`     (`token`),
  INDEX       `idx_refresh_email`    (`user_email`),
  INDEX       `idx_refresh_revoked`  (`revoked`),
  CONSTRAINT  `chk_refresh_user_type` CHECK (`user_type` IN ('EMPLOYEE','CANDIDATE','ADMIN'))
);


-- ────────────────────────────────────────────────────────────
-- 2. RBAC JUNCTION TABLES
--    Many-to-many: one user can have multiple roles.
--    Separated by user type to keep foreign keys clean.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `employee_roles` (
  `employee_id` BIGINT NOT NULL,
  `role_id`     BIGINT NOT NULL,
  PRIMARY KEY (`employee_id`, `role_id`),
  CONSTRAINT `fk_emp_role_employee`
    FOREIGN KEY (`employee_id`) REFERENCES `employees`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_emp_role_role`
    FOREIGN KEY (`role_id`)     REFERENCES `roles`(`id`)     ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `candidate_roles` (
  `candidate_id` BIGINT NOT NULL,
  `role_id`      BIGINT NOT NULL,
  PRIMARY KEY (`candidate_id`, `role_id`),
  CONSTRAINT `fk_can_role_candidate`
    FOREIGN KEY (`candidate_id`) REFERENCES `candidates`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_can_role_role`
    FOREIGN KEY (`role_id`)      REFERENCES `roles`(`id`)      ON DELETE CASCADE
);


-- ────────────────────────────────────────────────────────────
-- 3. SEED DEFAULT ROLES
--    INSERT IGNORE = skip silently if already exists (safe to re-run)
-- ────────────────────────────────────────────────────────────
INSERT IGNORE INTO `roles` (`name`, `description`, `created_at`) VALUES
  ('ROLE_EMPLOYEE',  'Standard employee — internal system access', NOW()),
  ('ROLE_CANDIDATE', 'Job applicant — limited portal access', NOW()),
  ('ROLE_ADMIN',     'Administrator — full system access', NOW());


-- ────────────────────────────────────────────────────────────
-- 4. FIX mail_logs STATUS CONSTRAINT
--    Original V1 constraint only allowed SENT | FAILED.
--    We need PENDING for async email queuing.
--    MySQL: must DROP then re-ADD the constraint.
-- ────────────────────────────────────────────────────────────
-- Drop only if exists (MySQL 8.0.19+)
SET @exist := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'mail_logs'
    AND CONSTRAINT_NAME = 'chk_mail_status'
);

SET @sqlstmt := IF(@exist > 0,
    'ALTER TABLE `mail_logs` DROP CONSTRAINT `chk_mail_status`',
    'SELECT 1'
);

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `mail_logs`
  ADD CONSTRAINT `chk_mail_status`
    CHECK (`status` IN ('PENDING', 'SENT', 'FAILED'));