package com.ecommerce.domain.user.dto.request;

import com.ecommerce.domain.user.entity.Role;
import com.ecommerce.domain.user.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 * 
 * 클라이언트 → 서버로 전달되는 데이터
 * 
 * 사용 예시:
 * POST /api/users/signup
 * {
 *   "email": "test@test.com",
 *   "password": "password123",
 *   "name": "홍길동"
 * }
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SignUpRequest {

    /**
     * 이메일
     * 
     * @NotBlank: 빈 문자열, null, 공백만 있는 문자열 불가
     * @Email: 이메일 형식 검증
     */
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    /**
     * 비밀번호
     * 
     * @Size: 최소 8자, 최대 20자
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
    private String password;
    
    /**
     * 이름
     * 
     * @Size: 최소 2자, 최대 10자
     */
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하여야 합니다")
    private String name;


     /**
     * DTO → Entity 변환
     * 
     * 사용 예시:
     * SignUpRequest request = new SignUpRequest("test@test.com", "1234", "홍길동");
     * User user = request.toEntity("암호화된비밀번호");
     * 
     * @param encodedPassword 암호화된 비밀번호
     * @return User 엔티티
     */
    public User toEntity(String encodePassword) {
        return User.builder()
                .email(this.email)
                .password(encodePassword) // 암호화된 비밀번호 사용!
                .name(this.name)
                .role(Role.USER) // 일반 사용자로 자동 설정
                .build();
    }
}
