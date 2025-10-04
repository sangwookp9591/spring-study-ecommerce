package com.ecommerce.domain.auth.exception;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;

/**
 * 이메일이 중복될 때 발생하는 예외
 * 
 * 사용 예시:
 * if (userRepository.existsByEmailAndDeletedFalse(email)) {
 * throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
 * }
 */
public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(ErrorCode.AUTH_DUPLICATE_EMAIL);
    }

    public DuplicateEmailException(String message) {
        super(ErrorCode.AUTH_DUPLICATE_EMAIL, message);
    }
}
