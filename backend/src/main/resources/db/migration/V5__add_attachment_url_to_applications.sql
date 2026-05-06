-- V5: Thêm cột attachment_url vào job_applications
ALTER TABLE job_applications
    ADD COLUMN IF NOT EXISTS attachment_url VARCHAR(500);
