INSERT INTO categories (name) VALUES
('식비'),
('카페·간식'),
('쇼핑'),
('이동·차량'),
('주거·생활요금'),
('건강·의료'),
('교육'),
('여가·취미'),
('금융'),
('기타')
ON DUPLICATE KEY UPDATE name = VALUES(name);