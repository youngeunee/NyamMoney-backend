-- classification dictionary table

CREATE TABLE IF NOT EXISTS merchant_dictionary (
  merchant_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  merchant_norm      VARCHAR(120) NOT NULL,      -- 정규화된 상호 (브랜드명)
  category_id        BIGINT UNSIGNED NOT NULL,   -- 카테고리 ID
  source             ENUM('RULE','LLM','MANUAL') NOT NULL DEFAULT 'RULE', -- 분류 출처
  confidence         DECIMAL(4,3) NOT NULL DEFAULT 1.000,  -- 신뢰도
  hit_count          INT UNSIGNED NOT NULL DEFAULT 1,  -- 사용 횟수
  last_seen_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 마지막 확인 시간
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at         DATETIME NULL,
  PRIMARY KEY (merchant_id),
  UNIQUE KEY uk_merchant_norm (merchant_norm),
  KEY idx_category_id (category_id)
);

CREATE TABLE brand_dictionary (
  brand_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  brand_name VARCHAR(120) NOT NULL,  -- 정규화된 브랜드명 (예: "스타벅스", "엽기떡볶이")
  category_id BIGINT UNSIGNED NOT NULL,  -- 카테고리 (예: 카페·간식, 식비 등)
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (brand_id),
  UNIQUE KEY uk_brand_name (brand_name)
);

CREATE TABLE brand_alias (
  alias_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  alias VARCHAR(120) NOT NULL,  -- 별칭 (예: "엽떡", "엽기떡볶이")
  brand_id BIGINT UNSIGNED NOT NULL,  -- 해당 별칭에 대응하는 브랜드
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (alias_id),
  UNIQUE KEY uk_alias (alias),
  FOREIGN KEY (brand_id) REFERENCES brand_dictionary(brand_id) ON DELETE CASCADE
);
