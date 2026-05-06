-- V904: Seed system settings (upsert — V901 đã insert 4 key, bổ sung 2 key mới)
INSERT INTO system_settings (setting_key, setting_value, description, updated_at)
VALUES
  ('MIN_JOB_BUDGET',        '100000', 'Ngân sách tối thiểu cho mỗi job (VND)',         CURRENT_TIMESTAMP),
  ('JOB_MAX_ATTACHMENTS',   '5',      'Số file đính kèm tối đa cho job',               CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO NOTHING;

-- Upsert lại các key từ V901 với key chuẩn UPPER_CASE (V901 dùng lower_case)
INSERT INTO system_settings (setting_key, setting_value, description, updated_at)
VALUES
  ('COMMISSION_RATE',          '10', 'Phần trăm hoa hồng nền tảng (%)',                CURRENT_TIMESTAMP),
  ('MAX_SUBMISSION_ATTEMPTS',  '3',  'Số lần freelancer được submit completion',       CURRENT_TIMESTAMP),
  ('AUTO_CONFIRM_DAYS',        '7',  'Số ngày sau submit tự động xác nhận',            CURRENT_TIMESTAMP),
  ('PAYMENT_TIMEOUT_HOURS',    '24', 'Hết hạn thanh toán contract (giờ)',              CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO NOTHING;
