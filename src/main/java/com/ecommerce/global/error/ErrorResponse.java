package com.ecommerce.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답 형식
 * 
 * 모든 에러는 이 형식으로 응답합니다.
 * 
 * 예시:
 * {
 *   "success": false,
 *   "code": "U001",
 *   "message": "사용자를 찾을 수 없습니다",
 *   "timestamp": "2025-10-01T12:00:00"
 * }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final boolean success = false;  // 항상 false
    private final String code;              // 에러 코드 (예: U001)
    private final String message;           // 에러 메시지
    private final LocalDateTime timestamp;  // 발생 시간
    private final List<FieldError> errors;  // 입력값 검증 에러 (있을 때만)
    
    /**
     * ErrorCode로부터 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * ErrorCode와 커스텀 메시지로 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 입력값 검증 에러용
     */
    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * FieldError DTO (입력값 검증 에러 상세 정보)
     */
    @Getter
    @Builder
    public static class FieldError {
        private final String field;          // 필드명 (예: email)
        private final String value;          // 입력값
        private final String reason;         // 에러 이유
        
        public static FieldError of(org.springframework.validation.FieldError error) {
            return FieldError.builder()
                    .field(error.getField())
                    .value(error.getRejectedValue() == null ? "" : error.getRejectedValue().toString())
                    .reason(error.getDefaultMessage())
                    .build();
        }
    }
}