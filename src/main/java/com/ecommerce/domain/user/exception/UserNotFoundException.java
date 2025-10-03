package com.ecommerce.domain.user.exception;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 * 
 * 사용 예시:
 * User user = userRepository.findById(id)
 * .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
 */
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }
}
