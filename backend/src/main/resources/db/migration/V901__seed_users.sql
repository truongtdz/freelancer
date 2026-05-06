-- V901: Seed users, profiles
-- Password plain: "Password123!"
-- BCrypt(10) hash (generated at build time):
-- $2a$10$5b.EsQOEPAp465MBqhRKNeR94arrwNfrhJRJMz1.ag.dH6v4/LGcC

INSERT INTO users (username, email, password, full_name, phone, role, status, email_verified, created_at, updated_at)
VALUES
  ('admin',
   'admin@freelance.local',
   '$2a$10$5b.EsQOEPAp465MBqhRKNeR94arrwNfrhJRJMz1.ag.dH6v4/LGcC',
   'System Admin', '0900000000', 'ADMIN', 'ACTIVE', TRUE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('client01',
   'client@freelance.local',
   '$2a$10$5b.EsQOEPAp465MBqhRKNeR94arrwNfrhJRJMz1.ag.dH6v4/LGcC',
   'Nguyễn Văn A', '0901111111', 'CLIENT', 'ACTIVE', TRUE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('freelance01',
   'freelancer@freelance.local',
   '$2a$10$5b.EsQOEPAp465MBqhRKNeR94arrwNfrhJRJMz1.ag.dH6v4/LGcC',
   'Trần Thị B', '0902222222', 'FREELANCER', 'ACTIVE', TRUE,
   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_profiles (user_id, bio, title, experience_years, hourly_rate, rating_avg, total_reviews, total_jobs_done)
SELECT
  id,
  CASE role
    WHEN 'ADMIN'      THEN 'Quản trị viên hệ thống TopFreelancer'
    WHEN 'CLIENT'     THEN 'Doanh nghiệp tìm kiếm freelancer chất lượng'
    WHEN 'FREELANCER' THEN 'Full-stack developer 5 năm kinh nghiệm'
  END,
  CASE role WHEN 'FREELANCER' THEN 'Senior Full-stack Developer' ELSE NULL END,
  CASE role WHEN 'FREELANCER' THEN 5 ELSE NULL END,
  CASE role WHEN 'FREELANCER' THEN 250000 ELSE NULL END,
  0, 0, 0
FROM users
WHERE email IN ('admin@freelance.local', 'client@freelance.local', 'freelancer@freelance.local')
ON CONFLICT (user_id) DO NOTHING;

-- System settings mặc định
INSERT INTO system_settings (setting_key, setting_value, description, updated_at)
VALUES
  ('commission_rate', '10', 'Phần trăm hoa hồng hệ thống (%)', CURRENT_TIMESTAMP),
  ('max_submission_attempts', '3', 'Số lần nộp bài tối đa', CURRENT_TIMESTAMP),
  ('contract_payment_timeout_hours', '24', 'Timeout thanh toán contract (giờ)', CURRENT_TIMESTAMP),
  ('auto_confirm_days', '7', 'Tự xác nhận hoàn thành sau N ngày', CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO NOTHING;
