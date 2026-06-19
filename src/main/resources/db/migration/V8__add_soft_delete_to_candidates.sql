ALTER TABLE candidates
    ADD COLUMN deleted_at DATETIME NULL,
    ADD COLUMN deleted_by VARCHAR(255) NULL;

-- The original status constraint (from V1) did not include DELETED.
-- Drop and recreate it so soft-deleted candidates can be persisted.
ALTER TABLE candidates DROP CHECK chk_candidate_status;
ALTER TABLE candidates ADD CONSTRAINT chk_candidate_status
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'TERMINATED', 'DELETED'));
