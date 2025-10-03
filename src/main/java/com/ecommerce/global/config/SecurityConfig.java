package com.ecommerce.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Spring Security 설정
 * 
 * 개발 단계: 모든 요청 허용
 * 나중에: JWT 인증 추가 예정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            // CSRF 비활성화 (REST API는 CSRF 불필요)
            .csrf(csrf -> csrf.disable())
            
            // 모든 요청 허용 (개발 단계)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
    
}
