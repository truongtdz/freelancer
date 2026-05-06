-- ============================================================
-- V908: Seed reviews, payment info, and user skills
-- ============================================================

-- ── Payment info for freelancer01 ────────────────────────────────────────────
INSERT INTO payment_info (user_id, bank_name, bank_account_number, bank_account_holder, qr_code_url, is_default, created_at)
SELECT u.id,
       'Vietcombank',
       '0011001234567',
       'TRAN THI B',
       'http://localhost:8080/uploads/qr-codes/seed-freelancer-qr.png',
       TRUE,
       NOW()
FROM users u
WHERE u.email = 'freelancer@freelance.local'
ON CONFLICT DO NOTHING;

-- ── Reviews for C-SEED-005 (PAID_OUT contract) ───────────────────────────────
-- Client → Freelancer
INSERT INTO reviews (contract_id, reviewer_id, reviewee_id, rating, comment, review_type, created_at)
SELECT c.id,
       c.client_id,
       c.freelancer_id,
       5,
       'Freelancer làm việc rất chuyên nghiệp, đúng deadline, chất lượng vượt mong đợi!',
       'CLIENT_TO_FREELANCER',
       NOW() - INTERVAL '12 hours'
FROM contracts c
WHERE c.contract_code = 'C-SEED-005'
ON CONFLICT ON CONSTRAINT reviews_contract_id_reviewer_id_key DO NOTHING;

-- Freelancer → Client
INSERT INTO reviews (contract_id, reviewer_id, reviewee_id, rating, comment, review_type, created_at)
SELECT c.id,
       c.freelancer_id,
       c.client_id,
       5,
       'Client trao đổi rõ ràng, feedback nhanh và thanh toán đúng hạn. Rất mong được hợp tác lần nữa!',
       'FREELANCER_TO_CLIENT',
       NOW() - INTERVAL '11 hours'
FROM contracts c
WHERE c.contract_code = 'C-SEED-005'
ON CONFLICT ON CONSTRAINT reviews_contract_id_reviewer_id_key DO NOTHING;

-- ── Update rating averages ────────────────────────────────────────────────────
UPDATE user_profiles
SET rating_avg    = 5.00,
    total_reviews = 1,
    total_jobs_done = 1
WHERE user_id = (SELECT id FROM users WHERE email = 'freelancer@freelance.local');

UPDATE user_profiles
SET rating_avg    = 5.00,
    total_reviews = 1
WHERE user_id = (SELECT id FROM users WHERE email = 'client@freelance.local');

-- ── User skills for freelancer01 ──────────────────────────────────────────────
INSERT INTO user_skills (user_id, skill_id, level)
SELECT u.id, s.id, vals.lvl
FROM users u
CROSS JOIN (
    VALUES
        ('java',        'EXPERT'),
        ('spring-boot', 'EXPERT'),
        ('angular',     'INTERMEDIATE'),
        ('postgresql',  'EXPERT')
) AS vals(slug, lvl)
JOIN skills s ON s.slug = vals.slug
WHERE u.email = 'freelancer@freelance.local'
ON CONFLICT (user_id, skill_id) DO UPDATE SET level = EXCLUDED.level;
