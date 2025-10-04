package com.ecommerce.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.domain.auth.dto.request.SignUpRequest;
import com.ecommerce.domain.auth.service.AuthService;
import com.ecommerce.domain.user.dto.response.UserResponse;
import com.ecommerce.global.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * 인증 컨트롤러
 * 
 * 엔드포인트:
 * - POST /api/auth/signup : 회원가입
 * - POST /api/auth/login : 로그인 (TODO)
 * - POST /api/auth/logout : 로그아웃 (TODO)
 * - POST /api/auth/refresh : 토큰 재발급 (TODO)
 * 
 * 왜 /api/auth?
 * 1. REST 표준
 * - /api/auth: 인증 액션
 * - /api/users: 사용자 리소스
 * 
 * 2. 대기업 관례
 * - 카카오: /api/v1/auth
 * - 네이버: /api/auth
 * - 배민: /api/auth
 * 
 * 3. 확장성
 * - OAuth: /api/auth/oauth/google
 * - SSO: /api/auth/sso
 * - 2FA: /api/auth/2fa
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * 
     * POST /api/users/signup
     * 
     * @param SignUpRequest
     * @return ApiResponse<UserResponse>
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("POST /api/users/signup - email: {}", request.getEmail());
        UserResponse response = authService.signUp(request);

        return ApiResponse.success("회원가입 성공", response);
    }

}
