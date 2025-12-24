package com.ssafy.project.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ssafy.project.security.filter.JwtVerificationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtVerificationFilter jwtVerificationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ CORS 설정: 프론트(5173)에서 백엔드(8080) 호출 허용
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 로컬 프론트 주소 허용
        // 5500 추가 WebSocket ..
        config.setAllowedOrigins(List.of(
                "http://localhost:5173", // 실제 Vue dev server
                "http://localhost:5500", // ws-test.html 테스트용
                "http://localhost:8080"  // (선택) 자기 자신
        ));

        // 허용 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용 헤더(Authorization 포함)
        config.setAllowedHeaders(List.of("*"));

        // 쿠키/인증정보 포함 허용
        config.setAllowCredentials(true);

        // 프론트에서 읽어야 하는 응답 헤더가 있으면 노출(선택)
        config.setExposedHeaders(List.of("Authorization", "Refresh-Token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ✅ CORS 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ✅ CORS preflight(OPTIONS)는 무조건 허용 (중요)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // webSocket 허용
                        .requestMatchers("/ws-challenge-chat").permitAll()
                        .requestMatchers("/ws-challenge-chat/**").permitAll()


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
                                "/api/v1/users/check-loginId",
                                "/api/v1/users/check-email"
                        )
                        .permitAll()

                        // ✅ 유저 관련 API는 로그인(토큰) 필요
                        //.requestMatchers("/api/v1/users/**").authenticated()

                        // ✅ 그 외도 인증 필요
                        //.anyRequest().authenticated()
                        .anyRequest().permitAll()
                )
                // 🔹 기본 로그인 기능 비활성화 (JWT만 사용)
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable())
                
                .exceptionHandling(ex -> ex
                	    .authenticationEntryPoint((req, res, e) -> {
                	        System.out.println("[SECURITY] 401 EntryPoint: " + e.getClass().getName() + " / " + e.getMessage());
                	        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                	    })
                	    .accessDeniedHandler((req, res, e) -> {
                	        System.out.println("[SECURITY] 403 AccessDenied: " + e.getClass().getName() + " / " + e.getMessage());
                	        res.sendError(HttpServletResponse.SC_FORBIDDEN);
                	    })
                	)
                
                // 🔹 UsernamePasswordAuthenticationFilter 전에 JWT 검증 필터를 태움
                .addFilterBefore(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class);
        		
        return http.build();
    }
}