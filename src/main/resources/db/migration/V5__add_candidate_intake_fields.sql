ALTER TABLE `candidates`
  ADD COLUMN `degree_in_us` TINYINT(1),
  ADD COLUMN `degree_other_notes` TEXT,
  ADD COLUMN `major` VARCHAR(255),
  ADD COLUMN `university_name` VARCHAR(255),
  ADD COLUMN `graduation_date` DATE,
  ADD COLUMN `date_of_birth` DATE,
  ADD COLUMN `work_auth_notes` TEXT,
  ADD COLUMN `tools_interested` TEXT,
  ADD COLUMN `needs_accommodation` TINYINT(1),
  ADD COLUMN `accommodation_move_in` DATE,
  ADD COLUMN `referral_source` VARCHAR(255),
  ADD COLUMN `experience_notes` TEXT,
  ADD COLUMN `resume_s3_key` TEXT,
  ADD COLUMN `ead_visa_s3_key` TEXT,
  ADD COLUMN `personal_email` VARCHAR(255);

CREATE INDEX `idx_candidates_work_authorization_status`
ON `candidates` (`work_authorization`, `status`);