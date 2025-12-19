SET NAMES utf8mb4;
SET time_zone = '+09:00';
-- SET SESSION sql_require_primary_key = 0;

CREATE DATABASE IF NOT EXISTS YumMoney
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE YumMoney;

-- 사용자
CREATE TABLE IF NOT EXISTS users (
  user_id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  login_id           VARCHAR(255) NOT NULL,
  email              VARCHAR(255) NOT NULL,
  pw_hash            VARCHAR(255) NOT NULL,
  nickname           VARCHAR(40)  NOT NULL,
  profile_visibility ENUM('PUBLIC', 'PROTECTED') NOT NULL DEFAULT 'PUBLIC',
  share_level        ENUM('NONE', 'SUMMARY', 'DETAIL') NOT NULL DEFAULT 'SUMMARY',
  timezone           VARCHAR(40) DEFAULT 'Asia/Seoul',
  monthly_budget     DECIMAL(12,2) DEFAULT 0,
  trigger_budget     DECIMAL(12,2) DEFAULT 0,
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at         DATETIME NULL,
  role				 ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
  name				 VARCHAR(50) NOT NULL,
  phone_number 		 VARCHAR(20),
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_users_email    (email),
  UNIQUE KEY uk_users_nickname (nickname)
  -- 필요하면 login_id도 UNIQUE로 추가 가능:
  -- ,UNIQUE KEY uk_users_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 리프레시 토큰
CREATE TABLE IF NOT EXISTS refresh_tokens (
  token_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  token_hash  VARCHAR(255) NOT NULL,
  expires_at  DATETIME NOT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (token_id),
  KEY idx_rt_user (user_id),
  CONSTRAINT fk_rt_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 카테고리
CREATE TABLE IF NOT EXISTS categories (
  category_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name        VARCHAR(50) NOT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (category_id),
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 트랜잭션(소비/수입/이체)
CREATE TABLE IF NOT EXISTS transactions (
  transaction_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id            BIGINT UNSIGNED NOT NULL,

  occurred_at        DATETIME NOT NULL,  -- 거래일시
  amount             DECIMAL(12,2) NOT NULL,  -- 금액
  tx_type            ENUM('INCOME','EXPENSE','TRANSFER') NOT NULL, -- 분류

  category_id        BIGINT UNSIGNED NULL,   -- 카테고리
  merchant_name_raw  VARCHAR(120) NULL,      -- 원본 가맹점명(AfstNm)

  payment_method     VARCHAR(30) NULL,       -- 카드/계좌/간편결제 등
  memo               TEXT NULL,
  tags               VARCHAR(255) NULL,      -- 태그(쉼표/JSON 등 원하는 포맷)

  -- 외부 연동 정보 (NH 카드 등)
  source             VARCHAR(16) NOT NULL DEFAULT 'manual', -- manual / nhcard / ...
  card_auth_no       VARCHAR(20) NULL,       -- 카드승인번호(CardAthzNo)
  sales_type         ENUM('ONETIME','INSTALLMENT','CASH',
                          'FOREIGN_ONETIME','FOREIGN_INSTALLMENT','FOREIGN_CASH') NULL, -- 매출종류(AmslKnd)
  installment_months TINYINT UNSIGNED NULL,  -- 할부개월수(Tris)
  currency_code      CHAR(3) NULL,           -- 통화코드(Crcd)

  -- 상태 플래그
  impulse_flag       TINYINT(1) NOT NULL DEFAULT 0,  -- 시발비용 여부
  is_refund          TINYINT(1) NOT NULL DEFAULT 0,  -- 취소/환불 여부(Ccyn 매핑)
  canceled_at        DATETIME NULL,                  -- 취소일자(CnclYmd)

  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at         DATETIME NULL,

  PRIMARY KEY (transaction_id),
  KEY idx_tx_user_time      (user_id, occurred_at),
  KEY idx_tx_user_cat_time  (user_id, category_id, occurred_at),

  CONSTRAINT fk_tx_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_tx_category FOREIGN KEY (category_id)
    REFERENCES categories(category_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 챌린지
CREATE TABLE IF NOT EXISTS challenges (
  challenge_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id        BIGINT UNSIGNED NOT NULL, -- 생성한 userId
  title        VARCHAR(120) NOT NULL, -- 예: "일주일 5만원"
  description VARCHAR(255) NOT NULL,
  budget_limit DECIMAL(12,2) NOT NULL,
  period_days  INT NOT NULL DEFAULT 7,
  starts_at    DATETIME NOT NULL,
  ends_at      DATETIME NOT NULL,
  status       ENUM('UPCOMING','ACTIVE','ENDED','CLOSED') NOT NULL DEFAULT 'UPCOMING',
  entry_fee    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (challenge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 챌린지 참가자
CREATE TABLE IF NOT EXISTS challenge_participants (
  participant_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  challenge_id   BIGINT UNSIGNED NOT NULL,
  user_id        BIGINT UNSIGNED NOT NULL,
  joined_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status         ENUM('JOINED','COMPLETED','FAILED','REFUNDED') NOT NULL DEFAULT 'JOINED',
  PRIMARY KEY (participant_id),
  UNIQUE KEY uk_ch_part  (challenge_id, user_id),
  KEY idx_chp_user       (user_id),
  CONSTRAINT fk_chp_ch   FOREIGN KEY (challenge_id) REFERENCES challenges(challenge_id) ON DELETE CASCADE,
  CONSTRAINT fk_chp_user FOREIGN KEY (user_id)      REFERENCES users(user_id)      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 챌린지별 소비 집계
CREATE TABLE IF NOT EXISTS challenge_spend_agg (
  spend_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  challenge_id  BIGINT UNSIGNED NOT NULL,
  user_id       BIGINT UNSIGNED NOT NULL,
  total_spend   DECIMAL(12,2) NOT NULL,
  impulse_spend DECIMAL(12,2) NOT NULL,
  calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (spend_id),
  UNIQUE KEY uk_ch_agg   (challenge_id, user_id),
  KEY idx_chagg_user     (user_id),
  CONSTRAINT fk_chagg_ch   FOREIGN KEY (challenge_id) REFERENCES challenges(challenge_id) ON DELETE CASCADE,
  CONSTRAINT fk_chagg_user FOREIGN KEY (user_id)      REFERENCES users(user_id)      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 게시판
CREATE TABLE IF NOT EXISTS boards (
  board_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  slug     VARCHAR(40) NOT NULL,
  name     VARCHAR(60) NOT NULL,
  PRIMARY KEY (board_id),
  UNIQUE KEY uk_boards_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 게시글
CREATE TABLE IF NOT EXISTS posts (
  post_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  board_id     BIGINT UNSIGNED NOT NULL,
  user_id      BIGINT UNSIGNED NOT NULL,
  challenge_id BIGINT UNSIGNED NULL,
  title        VARCHAR(140) NULL,
  content_md   MEDIUMTEXT NULL,
  likes_count  INT NOT NULL DEFAULT 0,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at   DATETIME NULL,
  PRIMARY KEY (post_id),
  KEY idx_posts_board_created (board_id, created_at),
  KEY idx_posts_user          (user_id),
  KEY idx_posts_ch            (challenge_id),
  CONSTRAINT fk_posts_board FOREIGN KEY (board_id)
    REFERENCES boards(board_id) ON DELETE CASCADE,
  CONSTRAINT fk_posts_user FOREIGN KEY (user_id)
    REFERENCES users(user_id)  ON DELETE CASCADE,
  CONSTRAINT fk_posts_ch FOREIGN KEY (challenge_id)
    REFERENCES challenges(challenge_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 게시글 좋아요
CREATE TABLE IF NOT EXISTS post_likes (
  liked_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id    BIGINT UNSIGNED NOT NULL,
  user_id    BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (liked_id),
  UNIQUE KEY uk_post_likes (post_id, user_id),
  KEY idx_pl_post (post_id),
  KEY idx_pl_user (user_id),
  CONSTRAINT fk_pl_post FOREIGN KEY (post_id)
    REFERENCES posts(post_id) ON DELETE CASCADE,
  CONSTRAINT fk_pl_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 댓글
CREATE TABLE IF NOT EXISTS comments (
  comment_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id    BIGINT UNSIGNED NOT NULL,
  user_id    BIGINT UNSIGNED NOT NULL,
  content_md TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (comment_id),
  KEY idx_cm_post (post_id),
  KEY idx_cm_user (user_id),
  CONSTRAINT fk_cm_post FOREIGN KEY (post_id)
    REFERENCES posts(post_id) ON DELETE CASCADE,
  CONSTRAINT fk_cm_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 일별 집계
CREATE TABLE IF NOT EXISTS user_daily_agg (
  report_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id        BIGINT UNSIGNED NOT NULL,
  day            DATE NOT NULL,
  total_spend    DECIMAL(12,2) NOT NULL DEFAULT 0,
  impulse_spend  DECIMAL(12,2) NOT NULL DEFAULT 0,
  dessert_spend  DECIMAL(12,2) NOT NULL DEFAULT 0,
  food_spend     DECIMAL(12,2) NOT NULL DEFAULT 0,
  shopping_spend DECIMAL(12,2) NOT NULL DEFAULT 0,
  etc_spend      DECIMAL(12,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (report_id),
  UNIQUE KEY uk_udagg_user_day (user_id, day),
  KEY idx_udagg_user (user_id),
  CONSTRAINT fk_udagg_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 월별 집계
CREATE TABLE IF NOT EXISTS user_monthly_agg (
  monthly_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id       BIGINT UNSIGNED NOT NULL,
  year          INT NOT NULL,
  month         INT NOT NULL,
  total_spend   DECIMAL(12,2) NOT NULL DEFAULT 0,
  impulse_ratio DECIMAL(5,2)  NOT NULL DEFAULT 0, -- %
  top_category  VARCHAR(50) NULL,
  summary_json  JSON NULL,
  PRIMARY KEY (monthly_id),
  UNIQUE KEY uk_umagg_user_ym (user_id, year, month),
  KEY idx_umagg_user (user_id),
  CONSTRAINT fk_umagg_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 팔로우
CREATE TABLE IF NOT EXISTS user_follows (
  follow_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  follower_id BIGINT UNSIGNED NOT NULL,  -- 나 (팔로우 하는 사람)
  followee_id BIGINT UNSIGNED NOT NULL,  -- 상대 (팔로우 대상)
  status      ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED') NOT NULL DEFAULT 'pending',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (follow_id),
  UNIQUE KEY uk_user_follows (follower_id, followee_id),
  KEY idx_uf_follower (follower_id),
  KEY idx_uf_followee (followee_id),
  CONSTRAINT fk_uf_follower FOREIGN KEY (follower_id)
    REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_uf_followee FOREIGN KEY (followee_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


















-- 감사 로그
CREATE TABLE IF NOT EXISTS audit_logs (
  log_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NULL,
  event      VARCHAR(60) NOT NULL, -- LOGIN, CREATE_TX, JOIN_CHALLENGE 등
  meta       JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (log_id),
  KEY idx_audit_user (user_id),
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 챌린지에 참가비 달았을 때.
CREATE TABLE IF NOT EXISTS payments (
  payment_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id      BIGINT UNSIGNED NOT NULL,
  challenge_id BIGINT UNSIGNED NULL,
  provider     VARCHAR(20) NULL,           -- toss/naverpay
  order_id     VARCHAR(64) NULL,
  amount       DECIMAL(12,2) NOT NULL,
  status       ENUM('PENDING','PAID','CANCELED','REFUNDED','FAILED')
               NOT NULL DEFAULT 'PENDING',
  raw_payload  JSON NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (payment_id),
  UNIQUE KEY uk_pay_order (order_id),
  KEY idx_pay_user (user_id),
  KEY idx_pay_ch   (challenge_id),
  CONSTRAINT fk_pay_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_pay_ch FOREIGN KEY (challenge_id)
    REFERENCES challenges(challenge_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
