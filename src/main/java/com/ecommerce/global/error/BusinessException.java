package com.ecommerce.global.error;

import lombok.Getter;

/**
 * 비즈니스 예외
 * 
 * 비즈니스 로직에서 발생하는 모든 예외의 부모 클래스
 * 
 * 사용 예시:
 * if (!userRepository.existsById(id)) {
 *     throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * }
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());  // 상위 클래스(RuntimeException)에 메시지 전달
        this.errorCode = errorCode;
    }
    
    // 메시지 커스터마이징이 필요한 경우
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}