package com.ssafy.project.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ssafy.project.api.v1.user.dto.UserDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JWTUtil {

    private final SecretKey key;

    // ✅ 실서비스: 매번 랜덤 key 생성 X, properties 에서 읽은 secret 으로 key 생성
    public JWTUtil(@Value("${ssafy.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.debug("jwt secret key length: {}", key.getEncoded().length);
    }

    @Value("${ssafy.jwt.access-expmin}")
    private long accessExpMin;

    @Value("${ssafy.jwt.refresh-expmin}")
    private long refreshExpMin;

    // ==== access / refresh 토큰 생성 메서드 ====

    public String createAccessToken(UserDto user) {
        Map<String, Object> claims = Map.of(
                "userId", user.getUserId(),
                "loginId", user.getLoginId(),
                "nickname", user.getNickname());
        return create("accessToken", accessExpMin, claims);
    }

    public String createRefreshToken(UserDto user) {
        Map<String, Object> claims = Map.of(
                "userId", user.getUserId(),
                "loginId", user.getLoginId(),
                "jti", UUID.randomUUID().toString());
        return create("refreshToken", refreshExpMin, claims);
    }

    /**
     * JWT 생성
     *
     * @param subject   token의 제목(accessToken, refreshToken)
     * @param expireMin 만료 시간(분)
     * @param claims    토큰에 담을 데이터
     */
    public String create(String subject, long expireMin, Map<String, Object> claims) {

        long now = System.currentTimeMillis();
        Date expireDate = new Date(now + 1000L * 60L * expireMin);

        String jwt = Jwts.builder()
                .subject(subject) // 토큰 용도
                .issuedAt(new Date(now)) // 발급 시간
                .expiration(expireDate) // 만료 시간
                .claims(claims) // 커스텀 클레임
                .signWith(key) // HS256 + secret key
                .compact();

        log.debug("jwt 토큰 발행 (subject: {}): {}", subject, jwt);
        return jwt;
    }

    /**
     * 토큰 검증 및 claim 정보 반환
     */
    public Claims getClaims(String jwt) {
        JwtParser parser = Jwts.parser()
                .verifyWith(key) // 서명 검증용 key
                .build();

        var jws = parser.parseSignedClaims(jwt);

        return jws.getPayload();
    }
}