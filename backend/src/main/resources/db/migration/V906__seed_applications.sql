-- V906: Seed job_applications for freelancer01
-- Mục tiêu: 12 applications trải đều status
--   8 PENDING  (job OPEN)
--   2 ACCEPTED (job IN_PROGRESS)
--   1 REJECTED
--   1 WITHDRAWN

-- -----------------------------------------------------------------------
-- Helper: get freelancer ID
-- -----------------------------------------------------------------------
-- (SELECT id FROM users WHERE email = 'freelancer@freelance.local') dùng inline

-- -----------------------------------------------------------------------
-- 8 PENDING — 8 job OPEN khác nhau
-- -----------------------------------------------------------------------

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Chào bạn! Tôi có 5 năm kinh nghiệm xây dựng landing page chuyển đổi cao, đã hoàn thành 30+ dự án tương tự. Tôi cam kết responsive, tốc độ load <2s và bàn giao đúng deadline.',
       (j.budget_min + j.budget_max) / 2,
       14,
       'PENDING',
       NOW() - INTERVAL '1 day'
FROM jobs j WHERE j.title = 'Xây dựng landing page bán hàng mỹ phẩm' AND j.deleted_at IS NULL;

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Tôi là Flutter developer 4 năm kinh nghiệm, đã ra mắt 10+ app trên cả Android & iOS. Quen với tích hợp POS và quản lý order cho F&B. Sẵn sàng bắt đầu ngay.',
       (j.budget_min + j.budget_max) / 2,
       21,
       'PENDING',
       NOW() - INTERVAL '2 days'
FROM jobs j WHERE j.title = 'Phát triển app Flutter cho cửa hàng cafe' AND j.deleted_at IS NULL;

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Tôi chuyên thiết kế thương hiệu cho startup giai đoạn seed & series A. Portfolio của tôi gồm 15 bộ nhận diện thương hiệu hoàn chỉnh, bao gồm fintech và edtech. Kết quả đo được bằng nhận diện thương hiệu tăng 40%.',
       (j.budget_min + j.budget_max) / 2,
       10,
       'PENDING',
       NOW() - INTERVAL '3 days'
FROM jobs j WHERE j.title = 'Thiết kế bộ nhận diện thương hiệu startup fintech' AND j.deleted_at IS NULL;

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Tôi viết nội dung tài chính cho 5+ trang web lớn tại Việt Nam, đạt top 3 Google cho nhiều từ khóa tài chính cạnh tranh. Bài viết chuẩn E-E-A-T, tối ưu on-page SEO.',
       (j.budget_min + j.budget_max) / 2,
       7,
       'PENDING',
       NOW() - INTERVAL '4 days'
FROM jobs j WHERE j.title = 'Viết 20 bài SEO chủ đề tài chính cá nhân' AND j.deleted_at IS NULL;

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Tôi có nền tảng kỹ thuật phần mềm + 3 năm kinh nghiệm dịch thuật EN-VN chuyên ngành IT. Cam kết thuật ngữ nhất quán và bàn giao đúng 50 trang trong 7 ngày.',
       (j.budget_min + j.budget_max) / 2,
       7,
       'PENDING',
       NOW() - INTERVAL '5 days'
FROM jobs j WHERE j.title = 'Dịch tài liệu kỹ thuật phần mềm EN-VN (50 trang)' AND j.deleted_at IS NULL;

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'DevOps 6 năm, đã setup CI/CD cho 20+ công ty từ startup đến doanh nghiệp. Thành thạo GitLab CI, Docker, Kubernetes. Sẽ cung cấp pipeline hoàn chỉnh kèm tài liệu và hướng dẫn vận hành.',
       (j.budget_min + j.budget_max) / 2,
       14,
       'PENDING',
       NOW() - INTERVAL '6 days'
FROM jobs j WHERE j.title = 'Setup CI/CD pipeline GitLab cho startup SaaS' AND j.deleted_at IS NULL;

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Data scientist 4 năm, chuyên NLP tiếng Việt. Đã publish nghiên cứu về phân tích cảm xúc bình luận mạng xã hội. Sẽ deliver mô hình accuracy >85% kèm API endpoint sẵn sàng production.',
       (j.budget_min + j.budget_max) / 2,
       20,
       'PENDING',
       NOW() - INTERVAL '7 days'
FROM jobs j WHERE j.title = 'Train mô hình NLP phân loại bình luận tiêu cực' AND j.deleted_at IS NULL;

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Tôi có 4 năm kinh nghiệm chạy quảng cáo Facebook cho FMCG, quản lý ngân sách 500tr+/tháng. ROAS trung bình đạt 4.5x. Sẽ lập kế hoạch chiến dịch chi tiết trong 48h đầu.',
       (j.budget_min + j.budget_max) / 2,
       30,
       'PENDING',
       NOW() - INTERVAL '2 days'
FROM jobs j WHERE j.title = 'Tư vấn chiến dịch quảng cáo Facebook cho FMCG' AND j.deleted_at IS NULL;

-- -----------------------------------------------------------------------
-- 2 ACCEPTED — 2 job IN_PROGRESS
-- -----------------------------------------------------------------------

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Java 21 & Spring Boot 3 là stack chính của tôi. Đã thực hiện 3 dự án migration tương tự, quen thuộc pitfall khi upgrade từ Java 8/11/17. Sẽ bàn giao test coverage ≥80%.',
       (j.budget_min + j.budget_max) / 2,
       21,
       'ACCEPTED',
       NOW() - INTERVAL '20 days',
       NOW() - INTERVAL '15 days'
FROM jobs j WHERE j.title = 'Nâng cấp hệ thống backend Spring Boot lên Java 21' AND j.deleted_at IS NULL;

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'UI/UX designer 5 năm, chuyên thiết kế app ERP & quản lý. Thành thạo Figma, đã làm việc với team dev để đảm bảo feasibility. Portfolio gồm 8 app quản lý kho/vận hành.',
       (j.budget_min + j.budget_max) / 2,
       14,
       'ACCEPTED',
       NOW() - INTERVAL '18 days',
       NOW() - INTERVAL '12 days'
FROM jobs j WHERE j.title = 'Thiết kế UI/UX app quản lý kho hàng' AND j.deleted_at IS NULL;

-- -----------------------------------------------------------------------
-- 1 REJECTED
-- -----------------------------------------------------------------------

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Tôi có kinh nghiệm làm WordPress 3 năm, đã build 20+ website tin tức và tạp chí online. Thành thạo theme customization, plugin SEO và tối ưu tốc độ.',
       (j.budget_min + j.budget_max) / 2,
       10,
       'REJECTED',
       NOW() - INTERVAL '12 days',
       NOW() - INTERVAL '10 days'
FROM jobs j WHERE j.title = 'Làm website tin tức bằng WordPress' AND j.deleted_at IS NULL;

-- -----------------------------------------------------------------------
-- 1 WITHDRAWN
-- -----------------------------------------------------------------------

INSERT INTO job_applications (job_id, freelancer_id, cover_letter, proposed_budget, proposed_duration_days, status, applied_at, responded_at)
SELECT j.id,
       (SELECT id FROM users WHERE email = 'freelancer@freelance.local'),
       'Laravel developer 4 năm. Đã debug và refactor nhiều legacy codebase Laravel 5/6/8. Có thể audit toàn bộ code và bàn giao danh sách issue kèm fix trong 7 ngày.',
       (j.budget_min + j.budget_max) / 2,
       7,
       'WITHDRAWN',
       NOW() - INTERVAL '8 days',
       NOW() - INTERVAL '6 days'
FROM jobs j WHERE j.title = 'Sửa lỗi và tối ưu backend Laravel' AND j.deleted_at IS NULL;

-- -----------------------------------------------------------------------
-- Index hint (nếu chưa có)
-- -----------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_applications_freelancer_status ON job_applications(freelancer_id, status);
