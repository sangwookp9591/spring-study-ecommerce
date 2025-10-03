package com.ecommerce.domain.user.dto.response;

import java.time.LocalDateTime;

import com.ecommerce.domain.user.entity.Role;
import com.ecommerce.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/**
 * 사용자 응답 DTO
 * 
 * 서버 → 클라이언트로 전달되는 데이터
 * 
 * 사용 예시:
 * {
 *   "id": 1,
 *   "email": "test@test.com",
 *   "name": "홍길동",
 *   "role": "USER",
 *   "createdAt": "2025-10-03T17:30:00"
 * }
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponse {

    
    /**
     * 사용자 ID
     */
    private Long id;
    
    /**
     * 이메일
     */
    private String email;
    
    /**
     * 이름
     */
    private String name;
    
    /**
     * 역할
     */
    private Role role;
    
    /**
     * 가입일시
     */
    private LocalDateTime createdAt;
    

    /**
     * Entity → DTO 변환
     * 
     * 사용 예시:
     * User user = userRepository.findById(1L).orElseThrow();
     * UserResponse response = UserResponse.from(user);
     * 
     * @param user User 엔티티
     * @return UserResponse DTO
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId()).email(user.getEmail())
            .name(user.getName())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .build();
    }
    
}
