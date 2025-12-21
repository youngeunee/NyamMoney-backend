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

