-- V903: Seed categories
INSERT INTO categories (name, slug, parent_id, status) VALUES
  ('Lập trình Web', 'lap-trinh-web', NULL, 'ACTIVE'),
  ('Lập trình Mobile', 'lap-trinh-mobile', NULL, 'ACTIVE'),
  ('Thiết kế đồ họa', 'thiet-ke-do-hoa', NULL, 'ACTIVE'),
  ('Viết nội dung', 'viet-noi-dung', NULL, 'ACTIVE'),
  ('Marketing', 'marketing', NULL, 'ACTIVE'),
  ('Dịch thuật', 'dich-thuat', NULL, 'ACTIVE'),
  ('Data & AI', 'data-ai', NULL, 'ACTIVE'),
  ('DevOps & Hệ thống', 'devops-he-thong', NULL, 'ACTIVE')
ON CONFLICT (slug) DO NOTHING;
