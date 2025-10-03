package com.ecommerce.global.config;

import com.ecommerce.global.security.jwt.JwtAuthenticationEntryPoint;
import com.ecommerce.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정
 * 
 * JWT 기반 인증/인가 설정을 담당합니다.
 * 
 * 주요 설정:
 * 1. JWT 인증 필터 등록
 * 2. CORS 설정
 * 3. CSRF 비활성화 (JWT 사용)
 * 4. Stateless 세션 정책
 * 5. URL별 접근 권한 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize, @Secured 어노테이션 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * 비밀번호 암호화 빈
     * 
     * BCrypt 알고리즘 사용:
     * - Salt 자동 생성
     * - 단방향 암호화
     * - 레인보우 테이블 공격 방어
     * 
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain 설정
     * 
     * Spring Security의 핵심 설정입니다.
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 설정 오류
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용하므로 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 사용 안 함 (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // 인증 실패 시 처리 (401)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // Public 엔드포인트 (인증 불필요)
                        .requestMatchers(
                                "/api/auth/**", // 로그인, 회원가입
                                "/api/users/signup", // 회원가입
                                "/error", // 에러 페이지
                                "/favicon.ico" // 파비콘
                        ).permitAll()

                        // Swagger (개발 환경)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**")
                        .permitAll()

                        // 관리자 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated())

                // JWT 인증 필터 추가
                // UsernamePasswordAuthenticationFilter 앞에 실행
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * 
     * Cross-Origin Resource Sharing 설정입니다.
     * 프론트엔드가 다른 도메인에서 API를 호출할 수 있도록 합니다.
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (프론트엔드 도메인)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000", // 로컬 개발
                "http://localhost:5173", // Vite
                "https://yourdomain.com" // 운영 도메인
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 인증 정보 포함 허용 (쿠키, Authorization 헤더)
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        // 노출할 헤더 (클라이언트가 접근 가능한 헤더)
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}