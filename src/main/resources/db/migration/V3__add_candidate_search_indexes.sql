-- Indexes to support candidate list search and filter queries.
-- candidate_id and email are already indexed via unique constraints added in V1.
CREATE INDEX idx_candidates_status     ON candidates(status);
CREATE INDEX idx_candidates_first_name ON candidates(first_name);
CREATE INDEX idx_candidates_last_name  ON candidates(last_name);
CREATE INDEX idx_candidates_phone      ON candidates(phone);