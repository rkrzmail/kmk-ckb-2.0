CREATE TABLE scheduler_logs (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(20) NOT NULL,
    error_message TEXT
);

CREATE INDEX idx_scheduler_logs_job_name ON scheduler_logs(job_name);