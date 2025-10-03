package com.ecommerce.global.security.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 인증 필터
 * 
 * 모든 HTTP 요청을 가로채서 JWT 토큰을 검증합니다.
 * - Authorization 헤더에서 토큰 추출
 * - 토큰 유효성 검증
 * - SecurityContext에 인증 정보 저장
 * 
 * OncePerRequestFilter:
 * - 요청당 1번만 실행 보장
 * - 동일한 요청이 여러 번 필터를 통과하는 것 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // Authorization 헤더 이름
    private static final String AUTHORIZATION_HEADER = "Authorization";
    // Bearer 타입 접두사
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 요청에서 JWT 토큰 추출
            String jwt = resolveToken(request);

            // 2. 토큰이 있고 유효한지 검증
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // 3. 토큰에서 인증 정보 추출
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);

                // 4. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Security Context에 '{}' 인증 정보 저장, uri: {}",
                        authentication.getName(), request.getRequestURI());
            } else {
                log.debug("유효한 JWT 토큰이 없습니다, uri: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("SecurityContext에서 사용자 인증 정보를 설정할 수 없습니다", e);
        }

        // 5. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);

    }

    /**
     * HTTP 요청 헤더에서 토큰 추출
     * 
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * ↑ 이 부분만 추출
     * 
     * @param request HTTP 요청
     * @return JWT 토큰 (Bearer 제외)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // "Bearer "로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            // "Bearer " 제거하고 토큰만 반환
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

}
