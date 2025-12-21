INSERT INTO users
(login_id, email, pw_hash, nickname, role, name, phone_number, monthly_budget, trigger_budget)
VALUES
('1', 'user01@test.com', '$2a$10$hash01', '민트초코', 'USER', '김민지', '010-1111-1111', 2000000, 500000),
('2', 'user02@test.com', '$2a$10$hash02', '라떼러버', 'USER', '이서준', '010-2222-2222', 2500000, 700000),
('3', 'user03@test.com', '$2a$10$hash03', '현명한소비', 'USER', '박지훈', '010-3333-3333', 3000000, 600000),
('admin','admin@test.com','$2a$10$hash99','관리자','ADMIN','관리자','010-9999-9999',0,0);

INSERT INTO categories (name)
VALUES
('식비'),
('카페·간식'),
('쇼핑'),
('이동·차량'),
('주거·생활요금'),
('건강·의료'),
('교육'),
('여가·취미'),
('금융'),
('기타');

INSERT INTO transactions
(user_id, occurred_at, amount, tx_type, category_id, merchant_name_raw, payment_method, memo, tags)
VALUES
(1, '2025-02-01 12:30:00', 12000, 'EXPENSE', 1, '김밥천국', 'CARD', '점심 식사', '식사,외식'),
(1, '2025-02-01 18:40:00', 4800, 'EXPENSE', 2, '스타벅스', 'CARD', '퇴근 후 커피', '카페'),
(2, '2025-02-02 09:10:00', 3200000, 'INCOME', NULL, '월급', 'ACCOUNT', '2월 급여', '급여'),
(2, '2025-02-02 20:00:00', 56000, 'EXPENSE', 3, '무신사', 'CARD', '의류 구매', '쇼핑,온라인'),
(3, '2025-02-03 07:50:00', 1350, 'EXPENSE', 4, '지하철', 'CARD', '출근', '교통');

INSERT INTO boards (slug, name)
VALUES
('free', '자유게시판'),
('tips', '팁 게시판'),
('qna', '질문 게시판'),
('notice', '공지 게시판');

INSERT INTO posts
(board_id, user_id, title, content_md, likes_count)
VALUES
(1, 1, '첫 글입니다!', '냠머니 시작했습니다. 잘 부탁드려요!', 3),
(1, 2, '소비 줄이는 팁 공유', '카페비 줄이는 방법 추천합니다.', 5),
(2, 3, '한 달 예산 관리 노하우', '카테고리별로 예산을 나누세요.', 7),
(3, 1, '이체 내역은 어떻게 관리하나요?', 'TRANSFER 거래 처리 방식이 궁금합니다.', 2),
(4, 4, '서비스 오픈 안내', '냠머니 베타 서비스를 시작합니다.', 10);

INSERT INTO user_follows
(follower_id, followee_id, status)
VALUES
(1, 2, 'ACCEPTED'),  -- 민트초코 → 라떼러버
(2, 1, 'ACCEPTED'),  -- 맞팔
(1, 3, 'PENDING'),   -- 민트초코 → 현명한소비
(3, 2, 'BLOCKED'),   -- 현명한소비 ⛔ 라떼러버
(2, 3, 'REJECTED');  -- 라떼러버 → 현명한소비

UPDATE users
SET pw_hash = '$2a$10$hf8XG/N8wLuukEoHAnr2auvV9AIKZy3TzKsDZMRF/2NL.6r5oPyp.'
WHERE user_id BETWEEN 1 AND 100;

select * from users;
