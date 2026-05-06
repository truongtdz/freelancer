-- V3: Contracts, progress reports, completion submissions
CREATE TABLE contracts (
    id               BIGSERIAL PRIMARY KEY,
    contract_code    VARCHAR(50)  UNIQUE NOT NULL,
    job_id           BIGINT       UNIQUE,
    application_id   BIGINT       UNIQUE,
    client_id        BIGINT       NOT NULL,
    freelancer_id    BIGINT       NOT NULL,
    agreed_price     DECIMAL(12, 2) NOT NULL,
    commission_rate  DECIMAL(5, 2),
    commission_amount DECIMAL(12, 2),
    net_amount       DECIMAL(12, 2),
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    actual_end_date  DATE,
    terms            TEXT,
    status           VARCHAR(30)  DEFAULT 'PENDING_PAYMENT'
                     CHECK (status IN (
                         'PENDING_PAYMENT', 'IN_PROGRESS', 'FREELANCER_SUBMITTED',
                         'CLIENT_CONFIRMED', 'PAID_OUT', 'DISPUTED', 'CANCELLED'
                     )),
    deleted_at       TIMESTAMP,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE progress_reports (
    id                  BIGSERIAL PRIMARY KEY,
    contract_id         BIGINT      NOT NULL,
    freelancer_id       BIGINT      NOT NULL,
    title               VARCHAR(255),
    content             TEXT,
    progress_percentage INT         CHECK (progress_percentage BETWEEN 0 AND 100),
    status              VARCHAR(30) DEFAULT 'SUBMITTED'
                        CHECK (status IN ('SUBMITTED', 'VIEWED_BY_CLIENT', 'FEEDBACK_RECEIVED')),
    client_feedback     TEXT,
    reported_at         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE progress_attachments (
    id                 BIGSERIAL PRIMARY KEY,
    progress_report_id BIGINT      NOT NULL,
    file_url           VARCHAR(500),
    file_name          VARCHAR(255),
    file_type          VARCHAR(50),
    uploaded_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- contract_id NOT UNIQUE — freelancer có thể submit nhiều lần (tối đa 3)
-- "submission hiện tại": WHERE contract_id=? ORDER BY attempt_number DESC LIMIT 1
-- record cũ chuyển status='SUPERSEDED' khi có submit mới
CREATE TABLE completion_submissions (
    id                  BIGSERIAL PRIMARY KEY,
    contract_id         BIGINT      NOT NULL,
    freelancer_id       BIGINT,
    summary             TEXT,
    deliverables_url    TEXT,
    payment_info_id     BIGINT,
    qr_code_url         VARCHAR(500),
    submitted_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    client_confirmed_at TIMESTAMP,
    status              VARCHAR(20) DEFAULT 'PENDING_CONFIRM'
                        CHECK (status IN ('PENDING_CONFIRM', 'CONFIRMED', 'REJECTED', 'SUPERSEDED')),
    rejection_reason    TEXT,
    attempt_number      INT         NOT NULL DEFAULT 1
);

-- Indexes
CREATE INDEX idx_contracts_client      ON contracts(client_id);
CREATE INDEX idx_contracts_freelancer  ON contracts(freelancer_id);
CREATE INDEX idx_contracts_status      ON contracts(status);
CREATE INDEX idx_contracts_job         ON contracts(job_id);
CREATE INDEX idx_progress_contract     ON progress_reports(contract_id);
CREATE INDEX idx_completion_contract   ON completion_submissions(contract_id, attempt_number DESC);
