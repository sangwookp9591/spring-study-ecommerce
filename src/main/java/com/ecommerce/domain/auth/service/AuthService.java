package com.ecommerce.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.domain.auth.dto.request.SignUpRequest;
import com.ecommerce.domain.auth.exception.DuplicateEmailException;
import com.ecommerce.domain.user.dto.response.UserResponse;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * 
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 정보
     * @throws DuplicateEmailException 이메일이 중복된 경우
     */
    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        log.info("회원가입 시도 : email={}", request.getEmail());

        // 1. 이메일 중복 체크
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            log.warn("이메일 중복 : {}", request.getEmail());
            throw new DuplicateEmailException("이미 사용중인 이메일입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("비밀번호 암호화 완료");

        // 3. User 엔티티 생성
        User user = request.toEntity(encodedPassword);

        // 4. DB저장 (publicId는 @PrePersist에서 자동 생성)
        User savedUser = userRepository.save(user);

        log.info("회원가입 성공: userId={} email={}", savedUser.getId(), savedUser.getEmail());

        // 4. Entity -> DTO 변환 후 반환
        return UserResponse.from(savedUser);
    }

}
