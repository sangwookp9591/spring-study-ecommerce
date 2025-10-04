package com.ecommerce.domain.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.domain.user.dto.response.UserResponse;
import com.ecommerce.domain.user.service.UserService;
import com.ecommerce.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 사용자 컨트롤러
 * 
 * HTTP 요청을 받아 Service로 전달하고, 응답을 반환합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * publicId로 사용자 조회
     * 
     * GET /api/users/{publicId}
     * 
     * @param publicId 사용자 공개 ID
     * @return 사용자 정보
     */
    @GetMapping("{publicId}")
    public ApiResponse<UserResponse> getUserByPublicId(@PathVariable String publicId) {
        log.info("GET /api/users/{}", publicId);
        UserResponse response = userService.findByPublicId(publicId);

        return ApiResponse.success("조회 성공", response);
    }

    /**
     * 이메일로 사용자 조회
     * 
     * GET /api/users/email/{email}
     * 
     * @param email 이메일
     * @return 사용자 정보
     */
    @GetMapping("/email/{email}")
    public ApiResponse<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("GET /api/users/email/{}", email);

        UserResponse response = userService.findByEmail(email);

        return ApiResponse.success("조회 성공", response);
    }

}
