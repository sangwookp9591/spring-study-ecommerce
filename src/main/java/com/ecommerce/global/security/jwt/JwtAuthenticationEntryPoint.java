package com.ecommerce.global.security.jwt;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.ecommerce.global.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 인증 실패 처리
 * 
 * 인증이 필요한 리소스에 인증 없이 접근할 때 호출됩니다.
 * 
 * 호출 시점:
 * 1. JWT 토큰이 없을 때
 * 2. JWT 토큰이 유효하지 않을 때
 * 3. JWT 토큰이 만료되었을 때
 * 4. 권한이 없을 때
 * 
 * 역할:
 * - 일관된 에러 응답 형식 제공
 * - 401 Unauthorized 응답
 * - JSON 형식으로 에러 정보 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 인증 실패 시 호출되는 메서드
     * 
     * Spring Security가 인증 실패를 감지하면 자동으로 호출합니다.
     * 
     * @param request       HTTP 요청
     * @param response      HTTP 응답
     * @param authException 인증 예외 (실패 원인)
     * @throws IOException      입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        // 인증 실패 로그 출력
        log.error("인증 실패: {} {}",
                request.getMethod(),
                request.getRequestURI(),
                authException);

        // 에러 응답 생성
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("AUTH001")
                .message("인증이 필요합니다. 로그인 후 다시 시도해주세요.")
                .build();

        // HTTP 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // application/json
        response.setCharacterEncoding("UTF-8");

        // JSON 응답 출력
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
