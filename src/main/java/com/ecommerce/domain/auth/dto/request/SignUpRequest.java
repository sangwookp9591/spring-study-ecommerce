package com.ecommerce.domain.auth.dto.request;

import com.ecommerce.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 * 
 * 클라이언트가 회원가입 시 전송하는 데이터
 * 
 * 왜 필요한가?
 * 1. User 엔티티와 분리
 * - User 엔티티는 DB 구조
 * - SignUpRequest는 API 스펙
 * - DB 변경 ≠ API 변경
 * 
 * 2. Validation
 * - API 레벨에서 입력값 검증
 * - 잘못된 데이터 조기 차단
 * - DB까지 가지 않고 막음
 * 
 * 3. 보안
 * - 필요한 필드만 받음
 * - id, publicId 등 생성 방지
 * - role 임의 설정 방지
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    /**
     * 이메일
     * 
     * @NotBlank: null, "", " " 모두 불허
     *            - null 체크
     *            - 빈 문자열 체크
     *            - 공백만 있는 문자열 체크
     * 
     * @Email: 이메일 형식 검증
     *         - @가 있는지
     *         - 도메인이 있는지
     *         - RFC 5322 표준
     * 
     *         왜 이렇게?
     *         - 이메일은 필수 (로그인 ID)
     *         - 잘못된 형식 조기 차단
     *         - DB 저장 전에 검증
     */
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    /**
     * 비밀번호
     * 
     * @NotBlank: 필수 입력
     * 
     * @Size: 길이 제한
     *        - min = 8: 최소 8자
     *        - max = 20: 최대 20자
     * 
     * @Pattern: 정규식 검증
     *           - (?=.*[a-z]): 소문자 1개 이상
     *           - (?=.*[A-Z]): 대문자 1개 이상
     *           - (?=.*\\d): 숫자 1개 이상
     *           - (?=.*[@$!%*?&]): 특수문자 1개 이상
     * 
     *           왜 이렇게?
     *           - 보안: 약한 비밀번호 방지
     *           - 대기업 표준: 복잡도 요구사항
     *           - 해킹 방지: 무차별 대입 공격 대비
     */
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다")
    private String password;

    /**
     * 이름
     * 
     * @NotBlank: 필수 입력
     * 
     * @Size: 길이 제한
     *        - min = 2: 최소 2자 (한글 1자 = 2~3byte)
     *        - max = 50: 최대 50자
     * 
     *        왜 이렇게?
     *        - 이름은 필수 (주문 배송 시 필요)
     *        - 너무 짧거나 긴 이름 방지
     *        - 비정상 입력 차단
     */
    @NotBlank(message = "이름을 입력해주세요")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    private String name;

    /**
     * User 엔티티로 변환
     * 
     * 왜 필요한가?
     * - DTO → Entity 변환
     * - Service에서 저장할 때 필요
     * - 변환 로직 한 곳에 모음
     * 
     * 왜 여기서 변환?
     * - DTO가 자신의 변환 책임
     * - Service 코드 간결
     * - 재사용 가능
     * 
     * @param encodedPassword 암호화된 비밀번호
     *                        - 평문 비밀번호 X
     *                        - BCrypt 암호화 필수
     *                        - Service에서 암호화 후 전달
     * @return User 엔티티
     */
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword) // 암호화된 비밀번호
                .name(this.name)
                .build();

        // 설정 안 하는 것들:
        // - id: DB가 자동 생성 (@GeneratedValue)
        // - publicId: User 엔티티 생성 시 자동 생성 (@PrePersist)
        // - role: User 엔티티에서 기본값 설정 (ROLE_USER)
        // - deleted: User 엔티티에서 기본값 false
        // - createdAt: @CreatedDate가 자동 설정
        // - updatedAt: @LastModifiedDate가 자동 설정
    }
}