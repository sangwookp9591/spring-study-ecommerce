package com.ecommerce.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
 * 애플리케이션 전체에서 발생하는 모든 예외를 여기서 잡아서 처리합니다.
 * 각 Controller에서 try-catch 할 필요가 없습니다!
 * 
 * @RestControllerAdvice
 * - 모든 @RestController에서 발생하는 예외를 잡음
 * - @ControllerAdvice + @ResponseBody
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 1. BusinessException 처리
     * 
     * 비즈니스 로직에서 발생하는 예외
     * 
     * 예시:
     * - 사용자를 찾을 수 없음
     * - 이메일 중복
     * - 재고 부족
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException", e);
        
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode);
        
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }
    
    /**
     * 2. @Valid 검증 실패 (MethodArgumentNotValidException)
     * 
     * @RequestBody에 @Valid를 붙였을 때 검증 실패 시 발생
     * 
     * 예시:
     * public void signUp(@Valid @RequestBody SignUpRequest request) {
     *     // email이 형식에 맞지 않으면 여기서 잡힘
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        
        // 어떤 필드에서 에러가 났는지 수집
        List<ErrorResponse.FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ErrorResponse.FieldError::of)
                .collect(Collectors.toList());
        
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    
    /**
     * 3. @ModelAttribute 검증 실패 (BindException)
     * 
     * GET 요청의 쿼리 파라미터 검증 실패 시 발생
     * 
     * 예시:
     * public void search(@Valid @ModelAttribute SearchRequest request) {
     *     // page가 0보다 작으면 여기서 잡힘
     * }
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.error("BindException", e);
        
        List<ErrorResponse.FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ErrorResponse.FieldError::of)
                .collect(Collectors.toList());
        
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    
    /**
     * 4. Enum 타입 불일치 (MethodArgumentTypeMismatchException)
     * 
     * 예시:
     * public void getUsers(@RequestParam Role role) {
     *     // role에 "INVALID_ROLE" 같은 값이 오면 여기서 잡힘
     * }
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException", e);
        
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    
    /**
     * 5. 지원하지 않는 HTTP 메서드 (HttpRequestMethodNotSupportedException)
     * 
     * 예시:
     * - POST /api/users로 요청해야 하는데 GET /api/users로 요청
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);
        
        ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
        
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }
    
    /**
     * 6. 예상하지 못한 모든 예외 (Exception)
     * 
     * 위에서 처리되지 않은 모든 예외를 여기서 잡음
     * 
     * 중요!
     * - 사용자에게는 일반적인 메시지만 보여줌 (보안)
     * - 실제 에러는 로그로 자세히 남김
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception", e);  // 스택 트레이스 전체 로깅
        
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}