package com.ecommerce.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * 
 * <p>
 * 애플리케이션 전체에서 발생하는 모든 예외를 중앙에서 처리합니다.
 * </p>
 * 
 * <h3>목적</h3>
 * <ul>
 * <li>각 Controller에서 try-catch 할 필요 없음</li>
 * <li>일관된 에러 응답 형식 제공</li>
 * <li>중복 코드 제거</li>
 * <li>보안 강화 (상세 에러 정보 숨김)</li>
 * </ul>
 * 
 * <h3>처리 흐름</h3>
 * 
 * <pre>
 * Controller에서 예외 발생
 *     ↓
 * GlobalExceptionHandler가 잡음
 *     ↓
 * ErrorCode 매핑
 *     ↓
 * ErrorResponse 생성
 *     ↓
 * JSON 응답 반환
 * </pre>
 * 
 * <h3>현업 대기업 특징</h3>
 * <ul>
 * <li>예외별 세밀한 처리</li>
 * <li>보안 로그 상세 기록</li>
 * <li>사용자에게는 추상적 메시지</li>
 * <li>개발자에게는 상세 로그</li>
 * </ul>
 * 
 * @RestControllerAdvice
 *                       - @ControllerAdvice + @ResponseBody
 *                       - 모든 @RestController의 예외를 잡음
 *                       - JSON 응답 자동 변환
 * 
 * @author ecommerce-team
 * @since 1.0
 * @see ErrorCode
 * @see ErrorResponse
 * @see BusinessException
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * BusinessException 처리
         * 
         * <p>
         * <b>우선순위:</b> 가장 먼저 확인 (가장 구체적)
         * </p>
         * 
         * <h3>발생 예외</h3>
         * <ul>
         * <li>DuplicateEmailException (AUTH-006)</li>
         * <li>InvalidCredentialsException (AUTH-001)</li>
         * <li>InvalidRefreshTokenException (AUTH-005)</li>
         * <li>UserNotFoundException (USER-001)</li>
         * <li>ProductNotFoundException (PRODUCT-001)</li>
         * <li>OrderNotFoundException (ORDER-001)</li>
         * </ul>
         * 
         * <h3>처리 과정</h3>
         * 
         * <pre>
         * 1. 예외에서 ErrorCode 추출
         *    throw new DuplicateEmailException();
         *    → ErrorCode.AUTH_DUPLICATE_EMAIL
         * 
         * 2. ErrorResponse 생성
         *    {
         *      "success": false,
         *      "code": "AUTH-006",
         *      "message": "이미 사용 중인 이메일입니다",
         *      "timestamp": "2025-10-03T15:30:00"
         *    }
         * 
         * 3. HTTP 상태 코드 설정
         *    ErrorCode의 HttpStatus 사용
         *    409 Conflict
         * </pre>
         * 
         * <h3>예시</h3>
         * 
         * <pre>
         * // AuthService.signUp()
         * if (userRepository.existsByEmail(email)) {
         *         throw new DuplicateEmailException(); // ← 여기서 발생
         * }
         * 
         * // GlobalExceptionHandler가 잡음
         * // 409 Conflict 응답
         * </pre>
         * 
         * @param e BusinessException
         * @return ErrorResponse with HTTP status
         */
        @ExceptionHandler(BusinessException.class)
        protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
                /**
                 * 로그 레벨: ERROR
                 * 
                 * 이유:
                 * - 비즈니스 로직 에러는 예상 가능한 상황
                 * - 하지만 추적은 필요 (통계, 모니터링)
                 * 
                 * 로그 내용:
                 * - 예외 타입
                 * - 에러 코드
                 * - 메시지
                 * - 스택 트레이스 (전체)
                 */
                log.error("BusinessException 발생 - code: {}, message: {}",
                                e.getErrorCode().getCode(),
                                e.getMessage(),
                                e);

                ErrorCode errorCode = e.getErrorCode();
                ErrorResponse response = ErrorResponse.of(errorCode);

                return ResponseEntity
                                .status(errorCode.getStatus())
                                .body(response);
        }

        /**
         * @Valid 검증 실패 처리 (MethodArgumentNotValidException)
         * 
         *        <p>
         *        <b>발생 시점:</b> @RequestBody + @Valid 검증 실패
         *        </p>
         * 
         *        <h3>검증 어노테이션</h3>
         *        <ul>
         *        <li>@NotBlank: 공백 체크</li>
         *        <li>@NotNull: null 체크</li>
         *        <li>@Email: 이메일 형식</li>
         *        <li>@Pattern: 정규식</li>
         *        <li>@Size: 길이 제한</li>
         *        <li>@Min, @Max: 숫자 범위</li>
         *        </ul>
         * 
         *        <h3>처리 과정</h3>
         * 
         *        <pre>
         * // 1. Controller 요청
         * POST /api/auth/signup
         * {
         *   "email": "invalid-email",  // ← @Email 위반
         *   "password": "123",         // ← @Size(min=8) 위반
         *   "name": ""                 // ← @NotBlank 위반
         * }
         * 
         * // 2. @Valid가 검증 실행
         * public void signUp(@Valid @RequestBody SignUpRequest request) {
         *     // 여기 도달 전에 예외 발생!
         * }
         * 
         * // 3. MethodArgumentNotValidException 발생
         * 
         * // 4. GlobalExceptionHandler가 잡음
         * 
         * // 5. 필드별 에러 수집
         * fieldErrors: [
         *   { field: "email", message: "올바른 이메일 형식이 아닙니다" },
         *   { field: "password", message: "비밀번호는 8자 이상이어야 합니다" },
         *   { field: "name", message: "이름을 입력해주세요" }
         * ]
         * 
         * // 6. 400 Bad Request 응답
         *        </pre>
         * 
         *        <h3>응답 예시</h3>
         * 
         *        <pre>
         * HTTP/1.1 400 Bad Request
         * {
         *   "success": false,
         *   "code": "COMMON-001",
         *   "message": "잘못된 입력값입니다",
         *   "timestamp": "2025-10-03T15:30:00",
         *   "fieldErrors": [
         *     {
         *       "field": "email",
         *       "value": "invalid-email",
         *       "reason": "올바른 이메일 형식이 아닙니다"
         *     },
         *     {
         *       "field": "password",
         *       "value": "123",
         *       "reason": "비밀번호는 8자 이상 20자 이하여야 합니다"
         *     },
         *     {
         *       "field": "name",
         *       "value": "",
         *       "reason": "이름을 입력해주세요"
         *     }
         *   ]
         * }
         *        </pre>
         * 
         *        <h3>프론트엔드 처리</h3>
         * 
         *        <pre>
         * if (error.code === 'COMMON-001') {
         *   // 필드별 에러 메시지 표시
         *   error.fieldErrors.forEach(fieldError => {
         *     showErrorOnField(fieldError.field, fieldError.reason);
         *   });
         * }
         *        </pre>
         * 
         * @param e MethodArgumentNotValidException
         * @return ErrorResponse with field errors (400 Bad Request)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException e) {

                /**
                 * 로그 레벨: WARN
                 * 
                 * 이유:
                 * - Validation 실패는 클라이언트 실수
                 * - ERROR보다는 낮은 레벨
                 * - 하지만 추적은 필요
                 */
                log.warn("Validation 실패 - {} errors", e.getBindingResult().getErrorCount());

                /**
                 * 필드 에러 수집
                 * 
                 * BindingResult에서 FieldError 추출:
                 * - field: 필드명 (email, password, ...)
                 * - rejectedValue: 입력된 값
                 * - defaultMessage: 에러 메시지
                 */
                List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(ErrorResponse.FieldError::of)
                                .collect(Collectors.toList());

                /**
                 * COMMON_INVALID_INPUT 사용
                 * 
                 * Before: INVALID_INPUT_VALUE (C001)
                 * After: COMMON_INVALID_INPUT (COMMON-001)
                 * 
                 * 변경 이유:
                 * - 도메인 명확화 (COMMON)
                 * - 네이밍 일관성
                 * - 확장성
                 */
                ErrorResponse response = ErrorResponse.of(
                                ErrorCode.COMMON_INVALID_INPUT,
                                fieldErrors);

                return ResponseEntity
                                .status(ErrorCode.COMMON_INVALID_INPUT.getStatus())
                                .body(response);
        }

        /**
         * @ModelAttribute 검증 실패 처리 (BindException)
         * 
         *                 <p>
         *                 <b>발생 시점:</b> GET 요청의 쿼리 파라미터 검증 실패
         *                 </p>
         * 
         *                 <h3>예시</h3>
         * 
         *                 <pre>
         * // Controller
         * &#64;GetMapping("/api/products/search")
         * public List<Product> search(@Valid @ModelAttribute SearchRequest request) {
         *     // page, size 등의 검증
         * }
         * 
         * // Request
         * GET /api/products/search?page=-1&size=1000
         * 
         * // SearchRequest
         * public class SearchRequest {
         *     &#64;Min(0)
         *     private int page;  // ← -1 위반!
         *     
         *     @Max(100)
         *     private int size;  // ← 1000 위반!
         * }
         * 
         * // 응답
         * 400 Bad Request
         * {
         *   "code": "COMMON-001",
         *   "fieldErrors": [
         *     { "field": "page", "reason": "0 이상이어야 합니다" },
         *     { "field": "size", "reason": "100 이하여야 합니다" }
         *   ]
         * }
         *                 </pre>
         * 
         * @param e BindException
         * @return ErrorResponse with field errors (400 Bad Request)
         */
        @ExceptionHandler(BindException.class)
        protected ResponseEntity<ErrorResponse> handleBindException(BindException e) {
                log.warn("BindException 발생 - {} errors", e.getBindingResult().getErrorCount());

                List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(ErrorResponse.FieldError::of)
                                .collect(Collectors.toList());

                ErrorResponse response = ErrorResponse.of(
                                ErrorCode.COMMON_INVALID_INPUT,
                                fieldErrors);

                return ResponseEntity
                                .status(ErrorCode.COMMON_INVALID_INPUT.getStatus())
                                .body(response);
        }

        /**
         * 타입 불일치 처리 (MethodArgumentTypeMismatchException)
         * 
         * <p>
         * <b>발생 시점:</b> 파라미터 타입 변환 실패
         * </p>
         * 
         * <h3>발생 케이스</h3>
         * 
         * <pre>
         * // Case 1: Enum 타입 불일치
         * &#64;GetMapping("/api/users")
         * public List<User> getUsers(@RequestParam Role role) {
         *     // role 파라미터에 INVALID_ROLE 같은 값
         * }
         * 
         * GET /api/users?role=INVALID_ROLE
         * → Role enum에 INVALID_ROLE 없음
         * → MethodArgumentTypeMismatchException 발생
         * 
         * // Case 2: 숫자 형식 오류
         * &#64;GetMapping("/api/products/{id}")
         * public Product getProduct(@PathVariable Long id) {
         *     // id에 "abc" 같은 문자열
         * }
         * 
         * GET /api/products/abc
         * → Long으로 변환 불가
         * → MethodArgumentTypeMismatchException 발생
         * 
         * // Case 3: Boolean 형식 오류
         * &#64;GetMapping("/api/products")
         * public List<Product> getProducts(@RequestParam boolean onSale) {
         *     // onSale에 "yes" 같은 값
         * }
         * 
         * GET /api/products?onSale=yes
         * → boolean 변환 불가 (true/false만 가능)
         * → MethodArgumentTypeMismatchException 발생
         * </pre>
         * 
         * <h3>응답 예시</h3>
         * 
         * <pre>
         * HTTP/1.1 400 Bad Request
         * {
         *   "success": false,
         *   "code": "COMMON-002",
         *   "message": "잘못된 데이터 타입입니다",
         *   "timestamp": "2025-10-03T15:30:00"
         * }
         * </pre>
         * 
         * @param e MethodArgumentTypeMismatchException
         * @return ErrorResponse (400 Bad Request)
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException e) {

                /**
                 * 로그에는 상세 정보 기록
                 * 
                 * 디버깅에 필요한 정보:
                 * - 파라미터명: e.getName()
                 * - 입력값: e.getValue()
                 * - 요구 타입: e.getRequiredType()
                 */
                log.warn("타입 불일치 - parameter: {}, value: {}, requiredType: {}",
                                e.getName(),
                                e.getValue(),
                                e.getRequiredType().getSimpleName());

                /**
                 * COMMON_INVALID_TYPE 사용
                 * 
                 * Before: INVALID_TYPE_VALUE (C004)
                 * After: COMMON_INVALID_TYPE (COMMON-002)
                 */
                ErrorResponse response = ErrorResponse.of(ErrorCode.COMMON_INVALID_TYPE);

                return ResponseEntity
                                .status(ErrorCode.COMMON_INVALID_TYPE.getStatus())
                                .body(response);
        }

        /**
         * 지원하지 않는 HTTP 메서드 처리 (HttpRequestMethodNotSupportedException)
         * 
         * <p>
         * <b>발생 시점:</b> 잘못된 HTTP 메서드 사용
         * </p>
         * 
         * <h3>예시</h3>
         * 
         * <pre>
         * // Controller 정의
         * &#64;PostMapping("/api/auth/login")  // ← POST만 허용
         * public LoginResponse login(@RequestBody LoginRequest request) {
         *     ...
         * }
         * 
         * // 잘못된 요청
         * GET /api/auth/login  // ← GET 사용
         * → HttpRequestMethodNotSupportedException 발생
         * 
         * // 응답
         * 405 Method Not Allowed
         * {
         *   "success": false,
         *   "code": "COMMON-003",
         *   "message": "지원하지 않는 HTTP 메서드입니다",
         *   "timestamp": "2025-10-03T15:30:00"
         * }
         * </pre>
         * 
         * <h3>HTTP 메서드별 용도</h3>
         * 
         * <pre>
         * GET: 조회 (Read)
         * POST: 생성 (Create)
         * PUT: 전체 수정 (Update)
         * PATCH: 부분 수정 (Partial Update)
         * DELETE: 삭제 (Delete)
         * </pre>
         * 
         * @param e HttpRequestMethodNotSupportedException
         * @return ErrorResponse (405 Method Not Allowed)
         */
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException e) {

                /**
                 * 로그에 상세 정보 기록
                 * 
                 * - 사용한 메서드: e.getMethod()
                 * - 지원하는 메서드: e.getSupportedMethods()
                 */
                log.warn("지원하지 않는 HTTP 메서드 - method: {}, supportedMethods: {}",
                                e.getMethod(),
                                e.getSupportedHttpMethods());

                /**
                 * COMMON_METHOD_NOT_ALLOWED 사용
                 * 
                 * Before: METHOD_NOT_ALLOWED (C002)
                 * After: COMMON_METHOD_NOT_ALLOWED (COMMON-003)
                 */
                ErrorResponse response = ErrorResponse.of(ErrorCode.COMMON_METHOD_NOT_ALLOWED);

                return ResponseEntity
                                .status(ErrorCode.COMMON_METHOD_NOT_ALLOWED.getStatus())
                                .body(response);
        }

        /**
         * 예상하지 못한 모든 예외 처리 (Exception)
         * 
         * <p>
         * <b>우선순위:</b> 가장 마지막 (가장 추상적)
         * </p>
         * 
         * <h3>처리 대상</h3>
         * <ul>
         * <li>위에서 처리되지 않은 모든 예외</li>
         * <li>NullPointerException</li>
         * <li>IllegalArgumentException</li>
         * <li>SQLException</li>
         * <li>IOException</li>
         * <li>RuntimeException</li>
         * <li>기타 모든 예외</li>
         * </ul>
         * 
         * <h3>보안 정책 (중요!) ⚠️</h3>
         * 
         * <pre>
         * 사용자에게 보이는 메시지:
         * ✅ "서버 오류가 발생했습니다"
         * ❌ "NullPointerException at line 42"
         * ❌ "Column 'user_email' not found"
         * 
         * 이유:
         * 1. 보안: 내부 구조 노출 방지
         * 2. UX: 사용자가 이해 못 함
         * 3. 안정성: 민감 정보 숨김
         * 
         * 개발자가 보는 로그:
         * ✅ 전체 스택 트레이스
         * ✅ 예외 타입
         * ✅ 예외 메시지
         * ✅ 발생 위치
         * </pre>
         * 
         * <h3>대응 프로세스</h3>
         * 
         * <pre>
         * 1. 즉시 알림
         *    - Slack, Email
         *    - Sentry, CloudWatch
         * 
         * 2. 로그 확인
         *    - 스택 트레이스 분석
         *    - 재현 조건 파악
         * 
         * 3. 긴급 패치
         *    - 핫픽스 배포
         *    - 모니터링 강화
         * 
         * 4. 사후 분석
         *    - 근본 원인 분석
         *    - 재발 방지 대책
         * </pre>
         * 
         * <h3>예시</h3>
         * 
         * <pre>
         * // 어딘가에서 NullPointerException 발생
         * User user = null;
         * String email = user.getEmail();  // ← NPE 발생!
         * 
         * // GlobalExceptionHandler가 잡음
         * 
         * // 사용자 응답 (간단)
         * 500 Internal Server Error
         * {
         *   "success": false,
         *   "code": "COMMON-004",
         *   "message": "서버 오류가 발생했습니다",
         *   "timestamp": "2025-10-03T15:30:00"
         * }
         * 
         * // 로그 (상세)
         * ERROR - Unexpected Exception
         * java.lang.NullPointerException: null
         *   at com.ecommerce.service.UserService.getEmail(UserService.java:42)
         *   at com.ecommerce.controller.UserController.getUser(UserController.java:25)
         *   ...
         * </pre>
         * 
         * @param e Exception
         * @return ErrorResponse (500 Internal Server Error)
         */
        @ExceptionHandler(Exception.class)
        protected ResponseEntity<ErrorResponse> handleException(Exception e) {

                /**
                 * 로그 레벨: ERROR
                 * 
                 * 이유:
                 * - 예상하지 못한 오류
                 * - 즉시 대응 필요
                 * - 서비스 영향 가능성
                 * 
                 * 로그 내용:
                 * - 전체 스택 트레이스 (e)
                 * - 예외 타입
                 * - 예외 메시지
                 */
                log.error("예상하지 못한 예외 발생 - type: {}, message: {}",
                                e.getClass().getSimpleName(),
                                e.getMessage(),
                                e); // ← 스택 트레이스 전체 출력

                /**
                 * COMMON_INTERNAL_ERROR 사용
                 * 
                 * Before: INTERNAL_SERVER_ERROR (C003)
                 * After: COMMON_INTERNAL_ERROR (COMMON-004)
                 * 
                 * 특징:
                 * - 500 Internal Server Error
                 * - 추상적인 메시지 (보안)
                 * - 상세 정보는 로그에만
                 */
                ErrorResponse response = ErrorResponse.of(ErrorCode.COMMON_INTERNAL_ERROR);

                return ResponseEntity
                                .status(ErrorCode.COMMON_INTERNAL_ERROR.getStatus())
                                .body(response);
        }
}