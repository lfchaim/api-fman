CREATE TABLE IF NOT EXISTS file_audit (
    id             BIGSERIAL       PRIMARY KEY,
    operation      VARCHAR(20)     NOT NULL,
    relative_path  VARCHAR(1024)   NOT NULL,
    file_name      VARCHAR(512)    NOT NULL,
    file_size      BIGINT,
    mime_type      VARCHAR(255),
    success        BOOLEAN         NOT NULL DEFAULT TRUE,
    error_message  TEXT,
    operated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    operated_by    VARCHAR(255)    NOT NULL DEFAULT 'system'
);

CREATE INDEX idx_file_audit_operation    ON file_audit(operation);
CREATE INDEX idx_file_audit_operated_at ON file_audit(operated_at DESC);
CREATE INDEX idx_file_audit_path        ON file_audit(relative_path);
