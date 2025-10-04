package com.ecommerce.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 * 
 * <p>
 * 현업 대기업 표준 + RFC 7807 기반 에러 코드 체계
 * </p>
 * 
 * <h3>네이밍 규칙</h3>
 * 
 * <pre>
 * {도메인}_{카테고리}_{번호}
 * 
 * 도메인:
 * - COMMON: 공통 (프레임워크, 시스템)
 * - AUTH: 인증/인가
 * - USER: 사용자 정보
 * - PRODUCT: 상품
 * - ORDER: 주문
 * - PAYMENT: 결제
 * 
 * 카테고리:
 * - INVALID: 유효하지 않음
 * - NOT_FOUND: 찾을 수 없음
 * - DUPLICATE: 중복
 * - EXPIRED: 만료
 * - FORBIDDEN: 권한 없음
 * 
 * 번호: 001부터 시작
 * </pre>
 * 
 * <h3>HTTP 상태 코드 매핑</h3>
 * 
 * <pre>
 * 400 Bad Request: 잘못된 요청 (INVALID_INPUT, INVALID_FORMAT)
 * 401 Unauthorized: 인증 필요 (TOKEN_MISSING, TOKEN_EXPIRED)
 * 403 Forbidden: 권한 없음 (FORBIDDEN, ACCESS_DENIED)
 * 404 Not Found: 리소스 없음 (NOT_FOUND)
 * 409 Conflict: 충돌 (DUPLICATE, ALREADY_EXISTS)
 * 500 Internal Server Error: 서버 오류 (INTERNAL_ERROR)
 * </pre>
 * 
 * <h3>사용 예시</h3>
 * 
 * <pre>
 * // Exception 발생
 * throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
 * 
 * // ErrorResponse 생성
 * ErrorResponse response = ErrorResponse.of(ErrorCode.USER_NOT_FOUND);
 * 
 * // HTTP 응답
 * return ResponseEntity
 *         .status(errorCode.getStatus())
 *         .body(ErrorResponse.of(errorCode));
 * </pre>
 * 
 * @author ecommerce-team
 * @since 1.0
 * @see BusinessException
 * @see ErrorResponse
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========================================
    // 공통 에러 (COMMON)
    // ========================================

    /**
     * COMMON_INVALID_INPUT
     * 
     * <p>
     * <b>HTTP 상태:</b> 400 Bad Request
     * </p>
     * <p>
     * <b>에러 코드:</b> COMMON-001
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>@Valid 검증 실패 (@NotBlank, @Email, @Pattern 등)</li>
     * <li>필수 파라미터 누락</li>
     * <li>잘못된 형식의 데이터 입력</li>
     * </ul>
     * 
     * <p>
     * <b>해결 방법:</b>
     * </p>
     * <ul>
     * <li>API 문서 확인</li>
     * <li>요청 파라미터 검증</li>
     * <li>FieldError 확인 (fieldErrors 필드)</li>
     * </ul>
     * 
     * <p>
     * <b>예시:</b>
     * </p>
     * 
     * <pre>
     * // 이메일 형식 오류
     * { "email": "invalid-email" }
     * 
     * // 비밀번호 규칙 위반
     * { "password": "123" }
     * </pre>
     */
    COMMON_INVALID_INPUT(
            HttpStatus.BAD_REQUEST,
            "COMMON-001",
            "잘못된 입력값입니다"),

    /**
     * COMMON_INVALID_TYPE
     * 
     * <p>
     * <b>HTTP 상태:</b> 400 Bad Request
     * </p>
     * <p>
     * <b>에러 코드:</b> COMMON-002
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>Enum 타입 변환 실패</li>
     * <li>숫자 형식 오류 (String → Integer)</li>
     * <li>날짜 형식 오류</li>
     * </ul>
     * 
     * <p>
     * <b>예시:</b>
     * </p>
     * 
     * <pre>
     * // Enum 오류
     * { "status": "INVALID_STATUS" }
     * 
     * // 숫자 형식 오류
     * { "price": "abc" }
     * </pre>
     */
    COMMON_INVALID_TYPE(
            HttpStatus.BAD_REQUEST,
            "COMMON-002",
            "잘못된 데이터 타입입니다"),

    /**
     * COMMON_METHOD_NOT_ALLOWED
     * 
     * <p>
     * <b>HTTP 상태:</b> 405 Method Not Allowed
     * </p>
     * <p>
     * <b>에러 코드:</b> COMMON-003
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>지원하지 않는 HTTP 메서드 사용</li>
     * <li>GET 엔드포인트에 POST 요청</li>
     * </ul>
     * 
     * <p>
     * <b>예시:</b>
     * </p>
     * 
     * <pre>
     * // POST로 정의된 엔드포인트에 GET 요청
     * GET /api/auth/login (실제는 POST)
     * </pre>
     */
    COMMON_METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "COMMON-003",
            "지원하지 않는 HTTP 메서드입니다"),

    /**
     * COMMON_INTERNAL_ERROR
     * 
     * <p>
     * <b>HTTP 상태:</b> 500 Internal Server Error
     * </p>
     * <p>
     * <b>에러 코드:</b> COMMON-004
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>예상치 못한 서버 오류</li>
     * <li>처리되지 않은 Exception</li>
     * <li>외부 API 장애</li>
     * </ul>
     * 
     * <p>
     * <b>대응:</b>
     * </p>
     * <ul>
     * <li>로그 확인 (Sentry, CloudWatch)</li>
     * <li>관리자에게 알림</li>
     * <li>장애 대응 프로세스 진행</li>
     * </ul>
     */
    COMMON_INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON-004",
            "서버 오류가 발생했습니다"),

    /**
     * COMMON_RESOURCE_NOT_FOUND
     * 
     * <p>
     * <b>HTTP 상태:</b> 404 Not Found
     * </p>
     * <p>
     * <b>에러 코드:</b> COMMON-005
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>존재하지 않는 엔드포인트 요청</li>
     * <li>잘못된 URL</li>
     * </ul>
     */
    COMMON_RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COMMON-005",
            "요청한 리소스를 찾을 수 없습니다"),

    // ========================================
    // 인증/인가 에러 (AUTH)
    // ========================================

    /**
     * AUTH_INVALID_CREDENTIALS
     * 
     * <p>
     * <b>HTTP 상태:</b> 401 Unauthorized
     * </p>
     * <p>
     * <b>에러 코드:</b> AUTH-001
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>로그인 시 이메일 또는 비밀번호 틀림</li>
     * <li>존재하지 않는 사용자</li>
     * <li>탈퇴한 사용자 (deleted = true)</li>
     * </ul>
     * 
     * <p>
     * <b>보안 정책:</b>
     * </p>
     * <ul>
     * <li>이메일 오류인지 비밀번호 오류인지 구분하지 않음</li>
     * <li>사용자 존재 여부 노출 방지 (보안)</li>
     * <li>Brute Force Attack 방지</li>
     * </ul>
     * 
     * <p>
     * <b>해결 방법:</b>
     * </p>
     * <ul>
     * <li>이메일/비밀번호 재확인</li>
     * <li>비밀번호 찾기 이용</li>
     * </ul>
     * 
     * <p>
     * <b>관련 기능:</b>
     * </p>
     * <ul>
     * <li>POST /api/auth/login</li>
     * <li>AuthService.login()</li>
     * </ul>
     */
    AUTH_INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "AUTH-001",
            "이메일 또는 비밀번호가 올바르지 않습니다"),

    /**
     * AUTH_TOKEN_REQUIRED
     * 
     * <p>
     * <b>HTTP 상태:</b> 401 Unauthorized
     * </p>
     * <p>
     * <b>에러 코드:</b> AUTH-002
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>Authorization 헤더 없이 인증 필요한 API 요청</li>
     * <li>Bearer 토큰 형식 오류</li>
     * </ul>
     * 
     * <p>
     * <b>해결 방법:</b>
     * </p>
     * 
     * <pre>
     * Authorization: Bearer {accessToken}
     * </pre>
     * 
     * <p>
     * <b>예시:</b>
     * </p>
     * 
     * <pre>
     * // 잘못된 요청
     * GET /api/users/me
     * (Authorization 헤더 없음)
     * 
     * // 올바른 요청
     * GET /api/users/me
     * Authorization: Bearer eyJhbGci...
     * </pre>
     */
    AUTH_TOKEN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH-002",
            "인증이 필요합니다. 로그인 후 다시 시도해주세요"),

    /**
     * AUTH_TOKEN_EXPIRED
     * 
     * <p>
     * <b>HTTP 상태:</b> 401 Unauthorized
     * </p>
     * <p>
     * <b>에러 코드:</b> AUTH-003
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>Access Token 만료 (30분 경과)</li>
     * <li>JWT의 exp claim이 현재 시간보다 이전</li>
     * </ul>
     * 
     * <p>
     * <b>해결 방법:</b>
     * </p>
     * 
     * <pre>
     * 1. Refresh Token으로 Access Token 재발급
     *    POST /api/auth/refresh
     *    { "refreshToken": "..." }
     * 
     * 2. 재발급 실패 시 재로그인
     *    POST /api/auth/login
     * </pre>
     * 
     * <p>
     * <b>프론트엔드 처리:</b>
     * </p>
     * 
     * <pre>
     * if (error.code === 'AUTH-003') {
     *   // 자동으로 토큰 재발급 시도
     *   const newToken = await refreshToken();
     *   // 원래 요청 재시도
     *   retry(originalRequest);
     * }
     * </pre>
     */
    AUTH_TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH-003",
            "토큰이 만료되었습니다. 다시 로그인해주세요"),

    /**
     * AUTH_TOKEN_INVALID
     * 
     * <p>
     * <b>HTTP 상태:</b> 401 Unauthorized
     * </p>
     * <p>
     * <b>에러 코드:</b> AUTH-004
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>JWT 서명 검증 실패 (변조된 토큰)</li>
     * <li>잘못된 형식의 JWT</li>
     * <li>다른 서버에서 발급한 JWT</li>
     * </ul>
     * 
     * <p>
     * <b>보안 경고:</b>
     * </p>
     * <ul>
     * <li>토큰 변조 시도 가능성</li>
     * <li>로그 기록 필수</li>
     * <li>IP 차단 고려</li>
     * </ul>
     */
    AUTH_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "AUTH-004",
            "유효하지 않은 토큰입니다"),

    /**
     * AUTH_REFRESH_TOKEN_INVALID
     * 
     * <p>
     * <b>HTTP 상태:</b> 401 Unauthorized
     * </p>
     * <p>
     * <b>에러 코드:</b> AUTH-005
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>Refresh Token 만료 (14일 경과)</li>
     * <li>Redis에 저장된 토큰과 불일치</li>
     * <li>로그아웃 후 사용 시도</li>
     * </ul>
     * 
     * <p>
     * <b>해결 방법:</b>
     * </p>
     * <ul>
     * <li>재로그인 필수</li>
     * <li>새로운 Access/Refresh Token 발급받기</li>
     * </ul>
     */
    AUTH_REFRESH_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "AUTH-005",
            "유효하지 않은 Refresh Token입니다. 다시 로그인해주세요"),

    /**
     * AUTH_DUPLICATE_EMAIL
     * 
     * <p>
     * <b>HTTP 상태:</b> 409 Conflict
     * </p>
     * <p>
     * <b>에러 코드:</b> AUTH-006
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>회원가입 시 이미 존재하는 이메일 사용</li>
     * <li>deleted = false인 사용자만 체크</li>
     * </ul>
     * 
     * <p>
     * <b>비즈니스 규칙:</b>
     * </p>
     * <ul>
     * <li>탈퇴한 사용자(deleted=true)의 이메일은 재사용 가능</li>
     * <li>활성 사용자(deleted=false) 중에서만 중복 체크</li>
     * </ul>
     * 
     * <p>
     * <b>해결 방법:</b>
     * </p>
     * <ul>
     * <li>다른 이메일 사용</li>
     * <li>해당 이메일로 로그인 시도</li>
     * <li>비밀번호 찾기 이용</li>
     * </ul>
     */
    AUTH_DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "AUTH-006",
            "이미 사용 중인 이메일입니다"),

    /**
     * AUTH_ACCOUNT_DISABLED
     * 
     * <p>
     * <b>HTTP 상태:</b> 403 Forbidden
     * </p>
     * <p>
     * <b>에러 코드:</b> AUTH-007
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>정지된 계정으로 로그인 시도</li>
     * <li>관리자가 계정 비활성화</li>
     * </ul>
     * 
     * <p>
     * <b>해결 방법:</b>
     * </p>
     * <ul>
     * <li>고객센터 문의</li>
     * <li>정지 사유 확인</li>
     * </ul>
     */
    AUTH_ACCOUNT_DISABLED(
            HttpStatus.FORBIDDEN,
            "AUTH-007",
            "비활성화된 계정입니다. 고객센터에 문의해주세요"),

    // ========================================
    // 사용자 에러 (USER)
    // ========================================

    /**
     * USER_NOT_FOUND
     * 
     * <p>
     * <b>HTTP 상태:</b> 404 Not Found
     * </p>
     * <p>
     * <b>에러 코드:</b> USER-001
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>존재하지 않는 publicId로 사용자 조회</li>
     * <li>탈퇴한 사용자 조회 (deleted = true)</li>
     * </ul>
     * 
     * <p>
     * <b>발생 위치:</b>
     * </p>
     * <ul>
     * <li>GET /api/users/{userId}</li>
     * <li>UserRepository.findByPublicIdAndDeletedFalse()</li>
     * </ul>
     */
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER-001",
            "사용자를 찾을 수 없습니다"),

    /**
     * USER_INVALID_PASSWORD
     * 
     * <p>
     * <b>HTTP 상태:</b> 401 Unauthorized
     * </p>
     * <p>
     * <b>에러 코드:</b> USER-002
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>사용자 정보 수정 시 현재 비밀번호 확인 실패</li>
     * <li>비밀번호 변경 시 기존 비밀번호 불일치</li>
     * </ul>
     * 
     * <p>
     * <b>참고:</b>
     * </p>
     * <ul>
     * <li>로그인 실패는 AUTH-001 사용</li>
     * <li>이 코드는 정보 수정 시에만 사용</li>
     * </ul>
     */
    USER_INVALID_PASSWORD(
            HttpStatus.UNAUTHORIZED,
            "USER-002",
            "현재 비밀번호가 일치하지 않습니다"),

    /**
     * USER_FORBIDDEN
     * 
     * <p>
     * <b>HTTP 상태:</b> 403 Forbidden
     * </p>
     * <p>
     * <b>에러 코드:</b> USER-003
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>다른 사용자의 정보 수정 시도</li>
     * <li>다른 사용자의 주문 조회 시도</li>
     * <li>관리자 권한 필요한 API에 일반 사용자 접근</li>
     * </ul>
     * 
     * <p>
     * <b>예시:</b>
     * </p>
     * 
     * <pre>
     * // 사용자 A가 사용자 B의 정보 수정 시도
     * PUT /api/users/user-b-id
     * Authorization: Bearer {user-a-token}
     * 
     * → 403 Forbidden (USER-003)
     * </pre>
     */
    USER_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "USER-003",
            "접근 권한이 없습니다"),

    // ========================================
    // 상품 에러 (PRODUCT)
    // ========================================

    /**
     * PRODUCT_NOT_FOUND
     * 
     * <p>
     * <b>HTTP 상태:</b> 404 Not Found
     * </p>
     * <p>
     * <b>에러 코드:</b> PRODUCT-001
     * </p>
     */
    PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PRODUCT-001",
            "상품을 찾을 수 없습니다"),

    /**
     * PRODUCT_OUT_OF_STOCK
     * 
     * <p>
     * <b>HTTP 상태:</b> 400 Bad Request
     * </p>
     * <p>
     * <b>에러 코드:</b> PRODUCT-002
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>재고 부족</li>
     * <li>주문 수량 > 재고 수량</li>
     * </ul>
     */
    PRODUCT_OUT_OF_STOCK(
            HttpStatus.BAD_REQUEST,
            "PRODUCT-002",
            "재고가 부족합니다"),

    // ========================================
    // 주문 에러 (ORDER)
    // ========================================

    /**
     * ORDER_NOT_FOUND
     * 
     * <p>
     * <b>HTTP 상태:</b> 404 Not Found
     * </p>
     * <p>
     * <b>에러 코드:</b> ORDER-001
     * </p>
     */
    ORDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "ORDER-001",
            "주문을 찾을 수 없습니다"),

    /**
     * ORDER_INVALID_STATUS
     * 
     * <p>
     * <b>HTTP 상태:</b> 400 Bad Request
     * </p>
     * <p>
     * <b>에러 코드:</b> ORDER-002
     * </p>
     * 
     * <p>
     * <b>발생 시점:</b>
     * </p>
     * <ul>
     * <li>배송 완료된 주문 취소 시도</li>
     * <li>이미 취소된 주문 취소 시도</li>
     * <li>잘못된 상태 전이 시도</li>
     * </ul>
     */
    ORDER_INVALID_STATUS(
            HttpStatus.BAD_REQUEST,
            "ORDER-002",
            "주문 상태가 올바르지 않습니다");

    // ========================================
    // 필드
    // ========================================

    /**
     * HTTP 상태 코드
     * 
     * Spring의 HttpStatus enum 사용
     * - 타입 안정성
     * - IDE 자동완성
     * - 표준 준수
     */
    private final HttpStatus status;

    /**
     * 커스텀 에러 코드
     * 
     * 형식: {도메인}-{번호}
     * - COMMON-001
     * - AUTH-001
     * - USER-001
     * 
     * 특징:
     * - 도메인별 구분 명확
     * - 확장 용이
     * - 검색 편리
     */
    private final String code;

    /**
     * 사용자에게 보여줄 에러 메시지
     * 
     * 특징:
     * - 한글로 작성 (i18n 고려 시 MessageSource 사용)
     * - 사용자 친화적
     * - 구체적이지만 보안 정보 노출 X
     */
    private final String message;

    // ========================================
    // 유틸리티 메서드 (선택)
    // ========================================

    /**
     * HTTP 상태 코드 숫자 반환
     * 
     * @return HTTP 상태 코드 (200, 400, 401, ...)
     */
    public int getStatusValue() {
        return status.value();
    }

    /**
     * 클라이언트 에러인지 확인 (4xx)
     * 
     * @return 4xx 여부
     */
    public boolean isClientError() {
        return status.is4xxClientError();
    }

    /**
     * 서버 에러인지 확인 (5xx)
     * 
     * @return 5xx 여부
     */
    public boolean isServerError() {
        return status.is5xxServerError();
    }
}