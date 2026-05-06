-- V905: Seed jobs (18 dòng: 10 OPEN, 3 IN_PROGRESS, 3 COMPLETED, 2 CANCELLED)

-- ===================== OPEN (10) =====================
INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='lap-trinh-web'),
  'Xây dựng landing page bán hàng mỹ phẩm',
  'Cần freelancer thiết kế + code landing page bán mỹ phẩm, responsive, tối ưu SEO. Yêu cầu kinh nghiệm Angular hoặc React, thiết kế chuẩn Figma.',
  3000000, 5000000, 'FIXED', NOW() + INTERVAL '30 days', 'REMOTE', 'Hà Nội', 'OPEN', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='lap-trinh-mobile'),
  'Phát triển app Flutter cho cửa hàng cafe',
  'Ứng dụng đặt món, quản lý bàn, thanh toán QR cho chuỗi cafe 5 chi nhánh. Cần tích hợp Firebase và REST API backend sẵn có.',
  8000000, 15000000, 'FIXED', NOW() + INTERVAL '45 days', 'REMOTE', 'TP.HCM', 'OPEN', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='thiet-ke-do-hoa'),
  'Thiết kế bộ nhận diện thương hiệu startup fintech',
  'Cần thiết kế logo, bộ màu, typography, mockup áp dụng trên web & mobile cho startup fintech. File Figma + AI/EPS.',
  5000000, 8000000, 'FIXED', NOW() + INTERVAL '20 days', 'REMOTE', 'Hà Nội', 'OPEN', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='viet-noi-dung'),
  'Viết 20 bài SEO chủ đề tài chính cá nhân',
  'Viết 20 bài dài 1200-1500 từ về đầu tư, tiết kiệm, tài chính cá nhân. Yêu cầu chuẩn SEO, dùng Surfer SEO outline.',
  2000000, 3000000, 'FIXED', NOW() + INTERVAL '15 days', 'REMOTE', NULL, 'OPEN', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='dich-thuat'),
  'Dịch tài liệu kỹ thuật phần mềm EN-VN (50 trang)',
  'Tài liệu API + hướng dẫn triển khai hệ thống phân tán. Cần người có kiến thức IT và tiếng Anh kỹ thuật tốt.',
  1500000, 2500000, 'FIXED', NOW() + INTERVAL '10 days', 'REMOTE', NULL, 'OPEN', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='devops-he-thong'),
  'Setup CI/CD pipeline GitLab cho startup SaaS',
  'Cần setup CI/CD GitLab (build, test, deploy lên K8s), viết Dockerfile chuẩn multi-stage, cấu hình staging + production.',
  4000000, 6000000, 'FIXED', NOW() + INTERVAL '14 days', 'REMOTE', NULL, 'OPEN', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='data-ai'),
  'Train mô hình NLP phân loại bình luận tiêu cực',
  'Xây dựng mô hình phân loại bình luận (positive/negative/neutral) từ 50K dữ liệu tiếng Việt. Dùng PhoBERT hoặc VnCoreNLP.',
  10000000, 20000000, 'FIXED', NOW() + INTERVAL '60 days', 'REMOTE', NULL, 'OPEN', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='marketing'),
  'Tư vấn chiến dịch quảng cáo Facebook cho FMCG',
  'Cần tư vấn + setup chiến dịch Facebook Ads cho sản phẩm tiêu dùng nhanh. Budget ads 50tr/tháng, target ROAS >3.',
  3000000, 5000000, 'FIXED', NOW() + INTERVAL '7 days', 'HYBRID', 'TP.HCM', 'OPEN', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='lap-trinh-web'),
  'Nâng cấp hệ thống backend Spring Boot lên Java 21',
  'Migration codebase từ Java 11/Spring Boot 2.x lên Java 21/Spring Boot 3.x. Đánh giá breaking changes, viết unit test coverage >80%.',
  6000000, 10000000, 'FIXED', NOW() + INTERVAL '30 days', 'REMOTE', NULL, 'OPEN', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='thiet-ke-do-hoa'),
  'Thiết kế UI/UX app quản lý kho hàng',
  'Thiết kế 30+ màn hình cho ứng dụng quản lý kho (nhập/xuất, kiểm kê, báo cáo). Bàn giao file Figma có component library.',
  4000000, 7000000, 'FIXED', NOW() + INTERVAL '25 days', 'REMOTE', NULL, 'OPEN', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day';

-- ===================== IN_PROGRESS (3) =====================
INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='lap-trinh-web'),
  'Làm website tin tức bằng WordPress',
  'Xây dựng website tin tức với WordPress, giao diện tùy chỉnh, SEO, tốc độ tải <3s. Tích hợp Google Analytics và Adsense.',
  4000000, 7000000, 'FIXED', NOW() + INTERVAL '20 days', 'REMOTE', NULL, 'IN_PROGRESS', NOW() - INTERVAL '15 days', NOW() - INTERVAL '5 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='lap-trinh-web'),
  'Sửa lỗi và tối ưu backend Laravel',
  'Fix 15 bug đã report trên Jira, tối ưu query chậm >500ms, viết unit test. Codebase Laravel 10 + MySQL.',
  1000000, 2000000, 'FIXED', NOW() + INTERVAL '7 days', 'REMOTE', NULL, 'IN_PROGRESS', NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='lap-trinh-mobile'),
  'Phát triển tính năng thanh toán cho app Android',
  'Tích hợp MoMo, ZaloPay, VNPay vào ứng dụng Android Native (Kotlin). Handle deep link callback, retry logic.',
  5000000, 8000000, 'FIXED', NOW() + INTERVAL '30 days', 'REMOTE', NULL, 'IN_PROGRESS', NOW() - INTERVAL '12 days', NOW() - INTERVAL '2 days';

-- ===================== COMPLETED (3) =====================
INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='thiet-ke-do-hoa'),
  'Thiết kế logo và namecard công ty công nghệ',
  'Logo + namecard 2 mặt cho startup công nghệ 10 người. Phong cách tối giản, hiện đại. Bàn giao AI + PNG.',
  800000, 1500000, 'FIXED', NULL, 'REMOTE', NULL, 'COMPLETED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '10 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='viet-noi-dung'),
  'Biên soạn nội dung website dịch vụ kế toán',
  '15 trang nội dung cho website kế toán: giới thiệu, dịch vụ, blog 5 bài. Chuẩn SEO onpage.',
  1500000, 2500000, 'FIXED', NULL, 'REMOTE', NULL, 'COMPLETED', NOW() - INTERVAL '45 days', NOW() - INTERVAL '20 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='devops-he-thong'),
  'Migrate database PostgreSQL lên AWS RDS',
  'Di chuyển 3 database PostgreSQL 11 lên AWS RDS PostgreSQL 16. Downtime tối đa 30 phút. Viết runbook đầy đủ.',
  3000000, 5000000, 'FIXED', NULL, 'REMOTE', NULL, 'COMPLETED', NOW() - INTERVAL '60 days', NOW() - INTERVAL '40 days';

-- ===================== CANCELLED (2) =====================
INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='marketing'),
  'Chạy chiến dịch Google Ads tháng 12',
  'Cần chuyên gia Google Ads chạy chiến dịch Search + Display tháng 12. Budget 20tr. Đã huỷ do ngân sách.',
  2000000, 3500000, 'FIXED', NULL, 'REMOTE', NULL, 'CANCELLED', NOW() - INTERVAL '40 days', NOW() - INTERVAL '35 days';

INSERT INTO jobs (client_id, category_id, title, description, budget_min, budget_max, budget_type, deadline, work_mode, location, status, created_at, updated_at)
SELECT
  (SELECT id FROM users WHERE email='client@freelance.local'),
  (SELECT id FROM categories WHERE slug='data-ai'),
  'Xây dựng dashboard phân tích dữ liệu bán hàng',
  'Dashboard Power BI / Metabase kết nối PostgreSQL, visualize KPI bán hàng theo ngày/tuần/tháng. Đã huỷ.',
  3000000, 5000000, 'FIXED', NULL, 'REMOTE', NULL, 'CANCELLED', NOW() - INTERVAL '50 days', NOW() - INTERVAL '45 days';

-- ===================== Job Skills =====================
INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Xây dựng landing page bán hàng mỹ phẩm' AND s.slug IN ('angular','react','figma');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Phát triển app Flutter cho cửa hàng cafe' AND s.slug IN ('flutter','postgresql');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Thiết kế bộ nhận diện thương hiệu startup fintech' AND s.slug IN ('figma','ui-ux-design');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Viết 20 bài SEO chủ đề tài chính cá nhân' AND s.slug IN ('content-writing','seo');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Dịch tài liệu kỹ thuật phần mềm EN-VN (50 trang)' AND s.slug IN ('content-writing');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Setup CI/CD pipeline GitLab cho startup SaaS' AND s.slug IN ('docker','kubernetes','aws');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Train mô hình NLP phân loại bình luận tiêu cực' AND s.slug IN ('python','mongodb');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Tư vấn chiến dịch quảng cáo Facebook cho FMCG' AND s.slug IN ('seo');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Nâng cấp hệ thống backend Spring Boot lên Java 21' AND s.slug IN ('java','spring-boot','postgresql');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Thiết kế UI/UX app quản lý kho hàng' AND s.slug IN ('figma','ui-ux-design');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Làm website tin tức bằng WordPress' AND s.slug IN ('seo','content-writing');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Sửa lỗi và tối ưu backend Laravel' AND s.slug IN ('mysql','postgresql');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Phát triển tính năng thanh toán cho app Android' AND s.slug IN ('react-native','flutter');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Thiết kế logo và namecard công ty công nghệ' AND s.slug IN ('figma','ui-ux-design');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Biên soạn nội dung website dịch vụ kế toán' AND s.slug IN ('content-writing','seo');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Migrate database PostgreSQL lên AWS RDS' AND s.slug IN ('postgresql','aws','docker');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Chạy chiến dịch Google Ads tháng 12' AND s.slug IN ('seo');

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id FROM jobs j, skills s
WHERE j.title = 'Xây dựng dashboard phân tích dữ liệu bán hàng' AND s.slug IN ('python','mongodb','postgresql');
