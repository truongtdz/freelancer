-- V2: Jobs, skills mapping, attachments, applications
CREATE TABLE jobs (
    id                 BIGSERIAL PRIMARY KEY,
    client_id          BIGINT       NOT NULL,
    category_id        BIGINT,
    title              VARCHAR(255) NOT NULL,
    description        TEXT         NOT NULL,
    requirements       TEXT,
    budget_min         DECIMAL(12, 2),
    budget_max         DECIMAL(12, 2),
    budget_type        VARCHAR(10)  CHECK (budget_type IN ('FIXED', 'HOURLY')),
    duration_days      INT,
    deadline           DATE,
    location           VARCHAR(255),
    work_mode          VARCHAR(10)  CHECK (work_mode IN ('REMOTE', 'ONSITE', 'HYBRID')),
    status             VARCHAR(20)  DEFAULT 'OPEN'
                       CHECK (status IN ('DRAFT', 'OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'CLOSED')),
    view_count         INT          DEFAULT 0,
    application_count  INT          DEFAULT 0,
    deleted_at         TIMESTAMP,
    created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_skills (
    job_id   BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (job_id, skill_id)
);

CREATE TABLE job_attachments (
    id          BIGSERIAL PRIMARY KEY,
    job_id      BIGINT       NOT NULL,
    file_url    VARCHAR(500),
    file_name   VARCHAR(255),
    file_size   BIGINT,
    uploaded_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_applications (
    id                     BIGSERIAL PRIMARY KEY,
    job_id                 BIGINT       NOT NULL,
    freelancer_id          BIGINT       NOT NULL,
    cover_letter           TEXT,
    proposed_budget        DECIMAL(12, 2),
    proposed_duration_days INT,
    status                 VARCHAR(20)  DEFAULT 'PENDING'
                           CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN')),
    applied_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    responded_at           TIMESTAMP,
    UNIQUE (job_id, freelancer_id)
);

-- Indexes
CREATE INDEX idx_jobs_status_created     ON jobs(status, created_at DESC);
CREATE INDEX idx_jobs_client             ON jobs(client_id);
CREATE INDEX idx_jobs_category           ON jobs(category_id);
CREATE INDEX idx_job_skills_skill        ON job_skills(skill_id);
CREATE INDEX idx_job_attachments_job     ON job_attachments(job_id);
CREATE INDEX idx_applications_job_status ON job_applications(job_id, status);
CREATE INDEX idx_applications_freelancer ON job_applications(freelancer_id);
