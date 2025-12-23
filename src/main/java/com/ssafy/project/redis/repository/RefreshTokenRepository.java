package com.ssafy.project.redis.repository;

import java.util.Map;
import java.time.Duration;


public interface RefreshTokenRepository {
    void replaceSingleSession(Long userId, String loginId, String deviceId,
                              String jti, Map<String, String> fields, Duration ttl);

    Map<Object, Object> findByJti(String jti);

    void deleteByJti(Long userId, String jti);

    void deleteAllByUserId(Long userId);
}
