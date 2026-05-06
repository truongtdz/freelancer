-- V907: Seed contracts (6 contracts — mỗi status 1 contract)
-- Dùng các job đã có trong V905 + tạo application inline khi cần.
-- freelancer = freelancer@freelance.local, client = client@freelance.local

-- ─────────────────────────────────────────────────────────────────────────
-- PHẦN 1: Tạo application dẫn đến contract (khi V906 chưa có)
-- ─────────────────────────────────────────────────────────────────────────

-- App cho contract PENDING_PAYMENT: 'Thiết kế bộ nhận diện thương hiệu startup fintech'
INSERT INTO job_applications
    (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Tôi đã thiết kế bộ nhận diện thương hiệu cho 10+ startup tại Việt Nam.',
       6000000, 10, 'ACCEPTED',
       NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour'
FROM jobs j WHERE j.title = 'Thiết kế bộ nhận diện thương hiệu startup fintech'
ON CONFLICT DO NOTHING;

-- App cho contract FREELANCER_SUBMITTED: 'Phát triển tính năng thanh toán cho app Android'
INSERT INTO job_applications
    (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Tôi đã tích hợp MoMo, ZaloPay, VNPay cho 5 app Android thương mại.',
       6500000, 25, 'ACCEPTED',
       NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days'
FROM jobs j WHERE j.title = 'Phát triển tính năng thanh toán cho app Android'
ON CONFLICT DO NOTHING;

-- App cho contract CLIENT_CONFIRMED: 'Thiết kế logo và namecard công ty công nghệ'
INSERT INTO job_applications
    (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Graphic designer 6 năm, chuyên logo và bộ nhận diện thương hiệu.',
       1200000, 7, 'ACCEPTED',
       NOW() - INTERVAL '35 days', NOW() - INTERVAL '32 days'
FROM jobs j WHERE j.title = 'Thiết kế logo và namecard công ty công nghệ'
ON CONFLICT DO NOTHING;

-- App cho contract PAID_OUT: 'Biên soạn nội dung website dịch vụ kế toán'
INSERT INTO job_applications
    (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Copywriter 4 năm chuyên B2B, dịch vụ tài chính & kế toán.',
       2000000, 10, 'ACCEPTED',
       NOW() - INTERVAL '50 days', NOW() - INTERVAL '48 days'
FROM jobs j WHERE j.title = 'Biên soạn nội dung website dịch vụ kế toán'
ON CONFLICT DO NOTHING;

-- App cho contract DISPUTED: 'Sửa lỗi và tối ưu backend Laravel'
INSERT INTO job_applications
    (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Laravel developer 4 năm, chuyên debug và tối ưu hệ thống.',
       1500000, 7, 'ACCEPTED',
       NOW() - INTERVAL '12 days', NOW() - INTERVAL '11 days'
FROM jobs j WHERE j.title = 'Sửa lỗi và tối ưu backend Laravel'
ON CONFLICT DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────
-- PHẦN 2: Contracts
-- ─────────────────────────────────────────────────────────────────────────

-- ── CONTRACT 1: PENDING_PAYMENT ─────────────────────────────────────────
INSERT INTO contracts (contract_code, job_id, application_id, client_id, freelancer_id,
    agreed_price, commission_rate, commission_amount, net_amount,
    start_date, end_date, status, created_at, updated_at)
SELECT 'C-SEED-001',
    j.id,
    (SELECT id FROM job_applications WHERE job_id = j.id AND status = 'ACCEPTED' ORDER BY id DESC LIMIT 1),
    j.client_id,
    (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
    6000000, 10, 600000, 5400000,
    CURRENT_DATE, CURRENT_DATE + INTERVAL '10 days',
    'PENDING_PAYMENT', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'
FROM jobs j WHERE j.title = 'Thiết kế bộ nhận diện thương hiệu startup fintech'
ON CONFLICT (contract_code) DO NOTHING;

-- ── CONTRACT 2: IN_PROGRESS ─────────────────────────────────────────────
INSERT INTO contracts (contract_code, job_id, application_id, client_id, freelancer_id,
    agreed_price, commission_rate, commission_amount, net_amount,
    start_date, end_date, status, created_at, updated_at)
SELECT 'C-SEED-002',
    j.id,
    (SELECT id FROM job_applications WHERE job_id = j.id AND status = 'ACCEPTED' ORDER BY id DESC LIMIT 1),
    j.client_id,
    (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
    8000000, 10, 800000, 7200000,
    CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE + INTERVAL '15 days',
    'IN_PROGRESS', NOW() - INTERVAL '15 days', NOW() - INTERVAL '10 days'
FROM jobs j WHERE j.title = 'Nâng cấp hệ thống backend Spring Boot lên Java 21'
ON CONFLICT (contract_code) DO NOTHING;

-- ── CONTRACT 3: FREELANCER_SUBMITTED ────────────────────────────────────
INSERT INTO contracts (contract_code, job_id, application_id, client_id, freelancer_id,
    agreed_price, commission_rate, commission_amount, net_amount,
    start_date, end_date, status, created_at, updated_at)
SELECT 'C-SEED-003',
    j.id,
    (SELECT id FROM job_applications WHERE job_id = j.id AND status = 'ACCEPTED' ORDER BY id DESC LIMIT 1),
    j.client_id,
    (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
    6500000, 10, 650000, 5850000,
    CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE + INTERVAL '5 days',
    'FREELANCER_SUBMITTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '2 days'
FROM jobs j WHERE j.title = 'Phát triển tính năng thanh toán cho app Android'
ON CONFLICT (contract_code) DO NOTHING;

-- ── CONTRACT 4: CLIENT_CONFIRMED ────────────────────────────────────────
INSERT INTO contracts (contract_code, job_id, application_id, client_id, freelancer_id,
    agreed_price, commission_rate, commission_amount, net_amount,
    start_date, end_date, status, created_at, updated_at)
SELECT 'C-SEED-004',
    j.id,
    (SELECT id FROM job_applications WHERE job_id = j.id AND status = 'ACCEPTED' ORDER BY id DESC LIMIT 1),
    j.client_id,
    (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
    1200000, 10, 120000, 1080000,
    CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '10 days',
    'CLIENT_CONFIRMED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '5 days'
FROM jobs j WHERE j.title = 'Thiết kế logo và namecard công ty công nghệ'
ON CONFLICT (contract_code) DO NOTHING;

-- ── CONTRACT 5: PAID_OUT ────────────────────────────────────────────────
INSERT INTO contracts (contract_code, job_id, application_id, client_id, freelancer_id,
    agreed_price, commission_rate, commission_amount, net_amount,
    start_date, end_date, status, created_at, updated_at)
SELECT 'C-SEED-005',
    j.id,
    (SELECT id FROM job_applications WHERE job_id = j.id AND status = 'ACCEPTED' ORDER BY id DESC LIMIT 1),
    j.client_id,
    (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
    2000000, 10, 200000, 1800000,
    CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE - INTERVAL '20 days',
    'PAID_OUT', NOW() - INTERVAL '45 days', NOW() - INTERVAL '1 day'
FROM jobs j WHERE j.title = 'Biên soạn nội dung website dịch vụ kế toán'
ON CONFLICT (contract_code) DO NOTHING;

-- ── CONTRACT 6: DISPUTED ────────────────────────────────────────────────
INSERT INTO contracts (contract_code, job_id, application_id, client_id, freelancer_id,
    agreed_price, commission_rate, commission_amount, net_amount,
    start_date, end_date, status, created_at, updated_at)
SELECT 'C-SEED-006',
    j.id,
    (SELECT id FROM job_applications WHERE job_id = j.id AND status = 'ACCEPTED' ORDER BY id DESC LIMIT 1),
    j.client_id,
    (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
    1500000, 10, 150000, 1350000,
    CURRENT_DATE - INTERVAL '12 days', CURRENT_DATE - INTERVAL '5 days',
    'DISPUTED', NOW() - INTERVAL '12 days', NOW() - INTERVAL '6 hours'
FROM jobs j WHERE j.title = 'Sửa lỗi và tối ưu backend Laravel'
ON CONFLICT (contract_code) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────
-- PHẦN 3: Transactions ESCROW SUCCESS cho contracts 2,3,4,5
-- ─────────────────────────────────────────────────────────────────────────
INSERT INTO transactions
    (transaction_code, contract_id, user_id, type, amount, currency, status, payment_method,
     vnp_txn_ref, vnp_response_code, created_at, completed_at)
SELECT 'TXN-' || c.contract_code, c.id, c.client_id,
    'ESCROW', c.agreed_price, 'VND', 'SUCCESS', 'VNPAY',
    'REF-' || c.contract_code, '00',
    NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'
FROM contracts c WHERE c.contract_code = 'C-SEED-002'
ON CONFLICT (transaction_code) DO NOTHING;

INSERT INTO transactions
    (transaction_code, contract_id, user_id, type, amount, currency, status, payment_method,
     vnp_txn_ref, vnp_response_code, created_at, completed_at)
SELECT 'TXN-' || c.contract_code, c.id, c.client_id,
    'ESCROW', c.agreed_price, 'VND', 'SUCCESS', 'VNPAY',
    'REF-' || c.contract_code, '00',
    NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days'
FROM contracts c WHERE c.contract_code = 'C-SEED-003'
ON CONFLICT (transaction_code) DO NOTHING;

INSERT INTO transactions
    (transaction_code, contract_id, user_id, type, amount, currency, status, payment_method,
     vnp_txn_ref, vnp_response_code, created_at, completed_at)
SELECT 'TXN-' || c.contract_code, c.id, c.client_id,
    'ESCROW', c.agreed_price, 'VND', 'SUCCESS', 'VNPAY',
    'REF-' || c.contract_code, '00',
    NOW() - INTERVAL '29 days', NOW() - INTERVAL '29 days'
FROM contracts c WHERE c.contract_code = 'C-SEED-004'
ON CONFLICT (transaction_code) DO NOTHING;

INSERT INTO transactions
    (transaction_code, contract_id, user_id, type, amount, currency, status, payment_method,
     vnp_txn_ref, vnp_response_code, created_at, completed_at)
SELECT 'TXN-' || c.contract_code, c.id, c.client_id,
    'ESCROW', c.agreed_price, 'VND', 'SUCCESS', 'VNPAY',
    'REF-' || c.contract_code, '00',
    NOW() - INTERVAL '44 days', NOW() - INTERVAL '44 days'
FROM contracts c WHERE c.contract_code = 'C-SEED-005'
ON CONFLICT (transaction_code) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────
-- PHẦN 4: Completion submission cho C-SEED-003 (FREELANCER_SUBMITTED)
-- ─────────────────────────────────────────────────────────────────────────
INSERT INTO completion_submissions
    (contract_id, freelancer_id, summary, deliverables_url, qr_code_url,
     status, attempt_number, submitted_at)
SELECT c.id, c.freelancer_id,
    'Đã hoàn thành tích hợp MoMo, ZaloPay, VNPay với deep link callback và retry logic đầy đủ. Kèm unit test coverage 85%. Mời client kiểm tra bản demo.',
    'https://github.com/seed/payment-android-deliverables',
    NULL,
    'PENDING_CONFIRM', 1,
    NOW() - INTERVAL '2 days'
FROM contracts c WHERE c.contract_code = 'C-SEED-003'
ON CONFLICT DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────
-- PHẦN 5: Payout cho C-SEED-005 (PAID_OUT)
-- ─────────────────────────────────────────────────────────────────────────
INSERT INTO payouts
    (payout_code, contract_id, freelancer_id, admin_id,
     gross_amount, commission_amount, net_amount, status, paid_at)
SELECT 'PAY-SEED-005', c.id, c.freelancer_id,
    (SELECT id FROM users WHERE email = 'admin@freelance.local'),
    c.agreed_price, c.commission_amount, c.net_amount,
    'COMPLETED', NOW() - INTERVAL '1 day'
FROM contracts c WHERE c.contract_code = 'C-SEED-005'
ON CONFLICT (payout_code) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────
-- PHẦN 6: Dispute cho C-SEED-006 (DISPUTED)
-- ─────────────────────────────────────────────────────────────────────────
INSERT INTO disputes
    (contract_id, raised_by_user_id, reason, description, status, created_at)
SELECT c.id, c.client_id,
    'Sản phẩm không đúng yêu cầu',
    'Freelancer đã nộp nhưng các bug liệt kê trong Jira vẫn chưa được fix. Query chậm vẫn còn. Đã yêu cầu sửa 2 lần nhưng không cải thiện. Mong admin xem xét và phân xử.',
    'OPEN',
    NOW() - INTERVAL '6 hours'
FROM contracts c WHERE c.contract_code = 'C-SEED-006'
ON CONFLICT DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────
-- PHẦN 7: Progress reports cho C-SEED-002 (IN_PROGRESS)
-- ─────────────────────────────────────────────────────────────────────────
INSERT INTO progress_reports
    (contract_id, freelancer_id, title, content, progress_percentage, reported_at)
SELECT c.id, c.freelancer_id,
    'Báo cáo tuần 1 — Phân tích breaking changes',
    'Đã phân tích toàn bộ breaking changes từ Spring Boot 2.x lên 3.x. Danh sách 23 điểm cần cập nhật. Đã sửa xong 10/23. Ước tính hoàn thành 50% cuối tuần này.',
    30,
    NOW() - INTERVAL '10 days'
FROM contracts c WHERE c.contract_code = 'C-SEED-002';

INSERT INTO progress_reports
    (contract_id, freelancer_id, title, content, progress_percentage, reported_at)
SELECT c.id, c.freelancer_id,
    'Báo cáo tuần 2 — Migration core xong 60%',
    'Đã hoàn thành migration security config, datasource, và JPA. Unit test đạt 72%. Phần còn lại là migration logging và cấu hình actuator.',
    60,
    NOW() - INTERVAL '3 days'
FROM contracts c WHERE c.contract_code = 'C-SEED-002';

-- ─────────────────────────────────────────────────────────────────────────
-- PHẦN 8: Cập nhật job status khớp với contract status
-- ─────────────────────────────────────────────────────────────────────────
UPDATE jobs SET status = 'IN_PROGRESS', updated_at = NOW()
WHERE title = 'Nâng cấp hệ thống backend Spring Boot lên Java 21'
  AND id IN (SELECT job_id FROM contracts WHERE contract_code = 'C-SEED-002');

UPDATE jobs SET status = 'IN_PROGRESS', updated_at = NOW()
WHERE title = 'Thiết kế bộ nhận diện thương hiệu startup fintech'
  AND id IN (SELECT job_id FROM contracts WHERE contract_code = 'C-SEED-001');
