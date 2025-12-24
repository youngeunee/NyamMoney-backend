package com.ssafy.project.redis.repository;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final StringRedisTemplate redis;
    
    public RefreshTokenRepositoryImpl(StringRedisTemplate redis) {
    	this.redis = redis;
    }
    
    private String keyJti(String jti) {
        return "refresh:jti:" + jti;
    }

    private String keyUser(Long userId) {
        return "refresh:user:" + userId;
    }

    /**
     * 단일 세션 정책:
     * - 해당 userId의 기존 jti 전부 삭제
     * - 새 jti 하나만 저장 + TTL
     * - user set에는 새 jti만 남김
     */
    @Override
    public void replaceSingleSession(Long userId, String loginId, String deviceId, String jti,
                                     Map<String, String> fields, Duration ttl) {

        if (userId == null || jti == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("invalid args for replaceSingleSession");
        }

        final String userKey = keyUser(userId);
        final String newJtiKey = keyJti(jti);

        redis.execute(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Object execute(org.springframework.data.redis.core.RedisOperations operations) throws DataAccessException {

                // 1) 기존 jti들 조회
                Set<String> oldJtis = (Set<String>) operations.opsForSet().members(userKey);
                if (oldJtis == null) oldJtis = Collections.emptySet();

                // 2) 트랜잭션 시작
                operations.multi();

                // 3) 기존 jti들의 메인 키 삭제
                if (!oldJtis.isEmpty()) {
                    Set<String> oldKeys = oldJtis.stream()
                            .map(oj -> "refresh:jti:" + oj)
                            .collect(Collectors.toSet());
                    operations.delete(oldKeys);
                }

                // 4) user set 비우기
                operations.delete(userKey);

                // 5) 새 jti hash 저장
                if (fields != null && !fields.isEmpty()) {
                    operations.opsForHash().putAll(newJtiKey, fields);
                }
                // (필수 필드 보장용) userId/loginId/deviceId를 여기서 강제로 넣고 싶으면 putAll 전에 fields에 세팅하세요.

                // 6) TTL 설정
                operations.expire(newJtiKey, ttl);

                // 7) user set에 새 jti 하나만 추가
                operations.opsForSet().add(userKey, jti);

                // 8) 커밋
                return operations.exec();
            }
        });
    }

    @Override
    public Map<Object, Object> findByJti(String jti) {
        if (jti == null) return null;
        String jtiKey = keyJti(jti);
        Map<Object, Object> map = redis.opsForHash().entries(jtiKey);
        return (map == null || map.isEmpty()) ? null : map;
    }

    @Override
    public void deleteByJti(Long userId, String jti) {
        if (userId == null || jti == null) return;
        String userKey = keyUser(userId);
        String jtiKey = keyJti(jti);

        redis.execute(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Object execute(org.springframework.data.redis.core.RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.delete(jtiKey);
                operations.opsForSet().remove(userKey, jti);
                return operations.exec();
            }
        });
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        if (userId == null) return;
        String userKey = keyUser(userId);

        redis.execute(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Object execute(org.springframework.data.redis.core.RedisOperations operations) throws DataAccessException {

                Set<String> jtis = (Set<String>) operations.opsForSet().members(userKey);
                if (jtis == null) jtis = Collections.emptySet();

                operations.multi();

                if (!jtis.isEmpty()) {
                    Set<String> keys = jtis.stream()
                            .map(j -> "refresh:jti:" + j)
                            .collect(Collectors.toSet());
                    operations.delete(keys);
                }

                operations.delete(userKey);
                return operations.exec();
            }
        });
    }

    // webSocket userId 찾기위해
    @Override
    public boolean existsByUserId(Long userId) {
        if (userId == null) return false;

        String userKey = "refresh:user:" + userId;

        Boolean exists = redis.hasKey(userKey);
        return Boolean.TRUE.equals(exists);
    }

}
