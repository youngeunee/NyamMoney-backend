package com.ssafy.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ssafy.project.security.filter.JwtVerificationFilter; // 🔹 네가 만든 필터

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 🔹 JwtVerificationFilter 주입
    private final JwtVerificationFilter jwtVerificationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Swagger는 항상 열어두기
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()

                        // ✅ 정적 리소스 / 에러 페이지 허용
                        .requestMatchers(
                                "/favicon.ico",
                                "/error")
                        .permitAll()

                        // ✅ 회원가입 / 로그인은 토큰 없이 가능
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/users/signup",
                                "/api/v1/auth/refresh",
                                "/api/v1/users/check-nickname",
                                "api/v1/users/check-loginid"
                        		)
                        .permitAll()

                        // ✅ 유저 관련 API는 로그인(토큰) 필요
                        .requestMatchers("/api/v1/users/**").authenticated()

                        // ✅ 그 외는 일단 전부 허용 (원하면 authenticated()로 바꾸면 됨)
                        .anyRequest().authenticated())
                // 🔹 기본 로그인 기능 비활성화 (JWT만 사용)
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable())
                // 🔹 UsernamePasswordAuthenticationFilter 전에 JWT 검증 필터를 태움
                .addFilterBefore(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}