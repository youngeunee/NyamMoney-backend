-- 브랜드 테이블 (brand_dictionary)

-- 식비 카테고리 (ID: 1)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('맥도날드', 1),  -- 패스트푸드
('롯데리아', 1),   -- 패스트푸드
('피자', 1), -- 패스트푸드
('버거킹', 1),     -- 패스트푸드
('BBQ', 1),        -- 치킨
('BHC', 1),
('파파존스', 1),
('KFC', 1),
('햄버거', 1),
('치킨', 1);    -- 치킨


-- 카페·간식 카테고리 (ID: 2)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('스타벅스', 2),   -- 카페
('카페', 2),
('텐퍼센트', 2),
('커피', 2),
('이디야', 2),
('공차', 2),
('투썸', 2),
('폴바셋', 2),
('더벤티', 2),
('메가MGC커피', 2),
('던킨도너츠', 2), -- 간식
('베스킨라빈스', 2), -- 간식
('파스쿠찌', 2),    -- 카페
('카페베네', 2);    -- 카페

-- 쇼핑 카테고리 (ID: 3)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('쿠팡', 3),       -- 쇼핑
('11번가', 3),     -- 쇼핑
('백화점', 3),  -- 쇼핑
('마트', 3),      -- 마트
('마켓', 3),
('SSG', 3),
('CU', 3),
('이마트24', 3);    -- 편의점

-- 이동·차량 카테고리 (ID: 4)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('타다', 4),       -- 차량
('카카오T', 4),    -- 차량
('쏘카', 4),       -- 차량
('우버', 4),
('교통대금', 4),
('버스', 4),
('택시', 4),
('현대자동차', 4); -- 차량

-- 주거·생활요금 카테고리 (ID: 5)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('SK텔레콤', 5),   -- 통신
('KT', 5),         -- 통신
('LG유플러스', 5), -- 통신
('이케아', 5);     -- 가구

-- 건강·의료 카테고리 (ID: 6)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('병원', 6),
('의원', 6),
('약국', 6);
-- ('서울대학교병원', 6),  -- 병원
-- ('삼성서울병원', 6),    -- 병원
-- ('한양대학교병원', 6);  -- 병원

-- 교육 카테고리 (ID: 7)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('토익', 7),             -- 교육
('YBM', 7),
('학원', 7);         -- 교육

-- 여가·취미 카테고리 (ID: 8)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('CGV', 8),      -- 영화관
('롯데시네마', 8), -- 영화관
('넷플릭스', 8),
('콘텐츠연합플랫폼', 8), -- 웨이브, 티빙
('DISNEY', 8), -- 디플
('카카오게임즈', 8), -- 게임
('넥슨', 8),         -- 게임
('넥스트레벨', 8);   -- 게임

-- 금융 카테고리 (ID: 9)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('카카오뱅크', 9),  -- 뱅킹
('국민은행', 9),    -- 뱅킹
('신한은행', 9),    -- 뱅킹
('삼성카드', 9),    -- 카드
('현대카드', 9);    -- 카드

-- 기타 카테고리 (ID: 10)
INSERT INTO brand_dictionary (brand_name, category_id) VALUES
('삼성전자', 10),  -- 전자제품
('LG전자', 10),    -- 전자제품
('하이마트', 10);  -- 전자제품
