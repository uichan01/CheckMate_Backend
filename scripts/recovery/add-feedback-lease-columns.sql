-- Apply once to an existing MySQL database before deploying the recovery-enabled consumer.
-- New databases created by Hibernate already include these columns.
ALTER TABLE task_submission_ai_feedbacks
    ADD COLUMN processing_token VARCHAR(36) NULL,
    ADD COLUMN lease_until DATETIME(6) NULL,
    ADD COLUMN processing_attempts INT NOT NULL DEFAULT 0;
