-- V4: Transactions, payouts, reviews, messaging, notifications, disputes, settings, audit, shedlock
CREATE TABLE transactions (
    id                   BIGSERIAL PRIMARY KEY,
    transaction_code     VARCHAR(50)  UNIQUE NOT NULL,
    contract_id          BIGINT,
    user_id              BIGINT,
    type                 VARCHAR(20)  CHECK (type IN ('DEPOSIT', 'ESCROW', 'PAYOUT', 'COMMISSION', 'REFUND')),
    amount               DECIMAL(12, 2) NOT NULL,
    currency             VARCHAR(10)  DEFAULT 'VND',
    status               VARCHAR(20)  DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'CANCELLED')),
    payment_method       VARCHAR(20)  CHECK (payment_method IN ('VNPAY', 'BANK_TRANSFER', 'QR_CODE', 'MANUAL')),
    vnp_txn_ref          VARCHAR(100) UNIQUE,
    vnp_transaction_no   VARCHAR(100),
    vnp_bank_code        VARCHAR(20),
    vnp_response_code    VARCHAR(10),
    vnp_response         TEXT,
    processed_by_admin_id BIGINT,
    note                 TEXT,
    created_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    completed_at         TIMESTAMP
);

CREATE TABLE payouts (
    id                  BIGSERIAL PRIMARY KEY,
    payout_code         VARCHAR(50)  UNIQUE NOT NULL,
    contract_id         BIGINT       UNIQUE,
    freelancer_id       BIGINT,
    admin_id            BIGINT,
    gross_amount        DECIMAL(12, 2),
    commission_amount   DECIMAL(12, 2),
    net_amount          DECIMAL(12, 2),
    qr_code_url         VARCHAR(500),
    bank_info_snapshot  JSONB,
    proof_image_url     VARCHAR(500),
    transaction_id      BIGINT,
    status              VARCHAR(20)  DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    paid_at             TIMESTAMP
);

CREATE TABLE reviews (
    id           BIGSERIAL PRIMARY KEY,
    contract_id  BIGINT,
    reviewer_id  BIGINT,
    reviewee_id  BIGINT,
    rating       INT         CHECK (rating BETWEEN 1 AND 5),
    comment      TEXT,
    review_type  VARCHAR(30) CHECK (review_type IN ('CLIENT_TO_FREELANCER', 'FREELANCER_TO_CLIENT')),
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (contract_id, reviewer_id)
);

CREATE TABLE conversations (
    id              BIGSERIAL PRIMARY KEY,
    contract_id     BIGINT,
    job_id          BIGINT,
    client_id       BIGINT,
    freelancer_id   BIGINT,
    last_message_at TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT,
    sender_id       BIGINT,
    content         TEXT,
    attachment_url  VARCHAR(500),
    is_read         BOOLEAN   DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT      NOT NULL,
    title          VARCHAR(255),
    content        TEXT,
    type           VARCHAR(30) CHECK (type IN (
                       'NEW_APPLICATION', 'APPLICATION_ACCEPTED',
                       'PAYMENT_RECEIVED', 'PROGRESS_REPORT', 'JOB_COMPLETED',
                       'PAYOUT_COMPLETED', 'NEW_MESSAGE', 'SYSTEM'
                   )),
    reference_type VARCHAR(50),
    reference_id   BIGINT,
    is_read        BOOLEAN     DEFAULT FALSE,
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE disputes (
    id                   BIGSERIAL PRIMARY KEY,
    contract_id          BIGINT,
    raised_by_user_id    BIGINT,
    reason               VARCHAR(255),
    description          TEXT,
    status               VARCHAR(20)  DEFAULT 'OPEN'
                         CHECK (status IN ('OPEN', 'IN_REVIEW', 'RESOLVED', 'CLOSED')),
    resolution           TEXT,
    resolved_by_admin_id BIGINT,
    created_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    resolved_at          TIMESTAMP
);

CREATE TABLE system_settings (
    id            BIGSERIAL PRIMARY KEY,
    setting_key   VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    description   VARCHAR(255),
    updated_by    BIGINT,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    action      VARCHAR(100),
    entity_type VARCHAR(50),
    entity_id   BIGINT,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(255),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ShedLock — bảng bắt buộc cho distributed scheduler
CREATE TABLE shedlock (
    name      VARCHAR(64)  PRIMARY KEY,
    lock_until TIMESTAMP   NOT NULL,
    locked_at  TIMESTAMP   NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);

-- Indexes
CREATE INDEX idx_transactions_contract   ON transactions(contract_id);
CREATE INDEX idx_transactions_status_type ON transactions(status, type);
CREATE INDEX idx_transactions_vnp_ref    ON transactions(vnp_txn_ref);
CREATE INDEX idx_payouts_freelancer      ON payouts(freelancer_id);
CREATE INDEX idx_reviews_reviewee        ON reviews(reviewee_id);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read, created_at DESC);
CREATE INDEX idx_disputes_status         ON disputes(status);
CREATE INDEX idx_audit_user              ON audit_logs(user_id, created_at DESC);
