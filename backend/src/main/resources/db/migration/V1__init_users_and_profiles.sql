-- V1: Users, profiles, skills, categories
CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50)  UNIQUE NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(100) NOT NULL,
    phone        VARCHAR(20),
    avatar_url   VARCHAR(500),
    role         VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'CLIENT', 'FREELANCER')),
    status       VARCHAR(20)  DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED')),
    email_verified BOOLEAN    DEFAULT FALSE,
    deleted_at   TIMESTAMP,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_profiles (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT    UNIQUE NOT NULL,
    bio              TEXT,
    title            VARCHAR(200),
    experience_years INT,
    hourly_rate      DECIMAL(12, 2),
    address          VARCHAR(255),
    website          VARCHAR(255),
    linkedin         VARCHAR(255),
    github           VARCHAR(255),
    rating_avg       DECIMAL(3, 2) DEFAULT 0,
    total_reviews    INT           DEFAULT 0,
    total_jobs_done  INT           DEFAULT 0
);

CREATE TABLE payment_info (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    bank_name           VARCHAR(100),
    bank_account_number VARCHAR(50),
    bank_account_holder VARCHAR(100),
    qr_code_url         VARCHAR(500),
    is_default          BOOLEAN      DEFAULT FALSE,
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE skills (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE user_skills (
    user_id  BIGINT      NOT NULL,
    skill_id BIGINT      NOT NULL,
    level    VARCHAR(20) CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'EXPERT')),
    PRIMARY KEY (user_id, skill_id)
);

CREATE TABLE categories (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    slug      VARCHAR(100) UNIQUE NOT NULL,
    parent_id BIGINT,
    icon      VARCHAR(255),
    status    VARCHAR(20)  DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- Indexes
CREATE INDEX idx_users_role_status    ON users(role, status);
CREATE INDEX idx_users_email          ON users(email);
CREATE INDEX idx_user_profiles_user   ON user_profiles(user_id);
CREATE INDEX idx_payment_info_user    ON payment_info(user_id);
CREATE INDEX idx_user_skills_skill    ON user_skills(skill_id);
CREATE INDEX idx_categories_parent    ON categories(parent_id);
