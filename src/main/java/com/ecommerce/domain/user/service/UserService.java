package com.ecommerce.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.domain.user.dto.response.UserResponse;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.exception.UserNotFoundException;
import com.ecommerce.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 서비스
 * 
 * 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * publicId로 사용자 조회
     * 
     * @param publicId 사용자 공개 ID
     * @return UserResponse
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserResponse findByPublicId(String publicId) {
        log.debug("사용자 조회 ");
        User user = userRepository.findByPublicIdAndDeletedFalse(publicId).orElseThrow(() -> {
            log.warn("사용자를 찾을 수 없음 : publicId={}", publicId);
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + publicId);
        });

        return UserResponse.from(user);
    }

    /**
     * 이메일로 이용자 정보 조회
     * 
     * @param email
     * @return UserResponse
     * @throws UserNotFoundException 이용자를 찾을 수 없음.
     */
    public UserResponse findByEmail(String email) {
        log.debug("사용자 조회: email={}", email);

        User user = userRepository.findByEmailAndDeletedFalse(email).orElseThrow(() -> {
            log.warn("사용자를 찾을 수 없음: email={}", email);
            throw new UserNotFoundException("사용자를 찾을 수 없습니다 : " + email);
        });

        return UserResponse.from((user));
    }

    /**
     * 비밀번호 검증
     * 
     * @param rawPassword     평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 일치 여부
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}
