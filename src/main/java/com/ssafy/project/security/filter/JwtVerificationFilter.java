package com.ssafy.project.security.filter;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ssafy.project.security.auth.UserPrincipal;
import com.ssafy.project.security.jwt.JWTUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("JwtVerificationFilter.doFilterInternal() called: {}", request.getRequestURI());

        String token = extractToken(request);

        // 토큰이 없으면 그냥 다음 필터로 진행 (비로그인 요청)
        if (token == null) {
            log.debug("[JWT] 토큰 없음, 다음 필터로 진행");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 토큰 검증 + 클레임 추출
            Claims claims = jwtUtil.getClaims(token);
            log.info("[JWT] 토큰 검증성공, claims = {}", claims);
            
            Long userId = claims.get("userId", Long.class);
            String loginId = claims.get("loginId", String.class);
            String nickname = (String) claims.get("nickname");

            // 2. Principal 생성
            UserPrincipal principal = new UserPrincipal(userId, loginId, nickname);

            // 3. Authentication 생성 (권한은 일단 비워둠)
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.emptyList()
                    );

            // 4. SecurityContext 세팅
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.warn("JWT 검증실패: {}", e.getMessage());
            // 실패해도 인증 없이 다음 필터 진행
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("accessToken".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
