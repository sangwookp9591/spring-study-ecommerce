package com.ecommerce.global.security.util;

import com.ecommerce.global.security.userdetails.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Optional;

/**
 * Security 유틸리티
 * 
 * SecurityContext에서 현재 인증된 사용자 정보를 쉽게 가져오는 유틸리티입니다.
 * 
 * 사용 예시:
 * - String userId = SecurityUtil.getCurrentUserId();
 * - String role = SecurityUtil.getCurrentUserRole();
 * - boolean isAdmin = SecurityUtil.hasRole("ADMIN");
 */
@Slf4j
public class SecurityUtil {

    /**
     * 현재 인증된 사용자의 Public ID 조회
     * 
     * @return 사용자 Public ID (UUID)
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static String getCurrentUserId() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다");
        }

        // authentication.getName()은 CustomUserDetails.getUsername()
        // = user.getPublicId()
        return authentication.getName();
    }

    /**
     * 현재 인증된 사용자의 Public ID 조회 (Optional)
     * 
     * 인증되지 않은 경우에도 예외를 발생시키지 않습니다.
     * 
     * @return Optional<사용자 Public ID>
     */
    public static Optional<String> getCurrentUserIdOptional() {
        try {
            return Optional.of(getCurrentUserId());
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    /**
     * 현재 인증된 사용자의 권한(Role) 조회
     * 
     * @return 권한 이름 (예: "ROLE_USER", "ROLE_ADMIN")
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static String getCurrentUserRole() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (authorities.isEmpty()) {
            throw new IllegalStateException("권한 정보가 없습니다");
        }

        // 첫 번째 권한 반환 (보통 하나만 있음)
        return authorities.iterator().next().getAuthority();
    }

    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인
     * 
     * @param role 권한 이름 (예: "ADMIN", "USER")
     * @return 권한 보유 여부
     */
    public static boolean hasRole(String role) {
        try {
            String currentRole = getCurrentUserRole();

            // "ROLE_" 접두사 처리
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            return currentRole.equals(role);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     * 
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 현재 인증된 사용자의 CustomUserDetails 조회
     * 
     * 더 많은 사용자 정보가 필요한 경우 사용합니다.
     * 
     * @return Optional<CustomUserDetails>
     */
    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return Optional.of((CustomUserDetails) principal);
        }

        return Optional.empty();
    }

    /**
     * 현재 Authentication 객체 조회
     * 
     * @return Authentication (인증 정보)
     */
    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * SecurityContext 초기화
     * 
     * 주로 테스트나 로그아웃 시 사용
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}