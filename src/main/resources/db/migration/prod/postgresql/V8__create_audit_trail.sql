CREATE TABLE IF NOT EXISTS audit_trail
(
    audit_trail_id BIGSERIAL PRIMARY KEY,
    trace_id       VARCHAR(100),
    actor_type     VARCHAR(30),
    actor_username VARCHAR(100),
    actor_id       VARCHAR(100),
    source_ip      VARCHAR(100),
    user_agent     TEXT,
    module_name    VARCHAR(100),
    action         VARCHAR(50),
    entity_name    VARCHAR(100),
    entity_id      VARCHAR(100),
    before_data    JSONB,
    after_data     JSONB,
    request_path   VARCHAR(300),
    http_method    VARCHAR(20),
    response_status INTEGER,
    success        BOOLEAN,
    error_message  TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_trail_created_at ON audit_trail(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_trail_actor_username ON audit_trail(actor_username);
CREATE INDEX IF NOT EXISTS idx_audit_trail_module_action ON audit_trail(module_name, action);
CREATE INDEX IF NOT EXISTS idx_audit_trail_entity ON audit_trail(entity_name, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_trail_trace_id ON audit_trail(trace_id);
