package com.ecommerce.global.security.userdetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.exception.UserNotFoundException;
import com.ecommerce.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security의 UserDetailsService 구현체
 * 
 * Spring Security가 사용자를 조회할 때 사용합니다.
 * 주로 로그인 시 username(여기서는 email)으로 사용자를 찾습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 조회
     * 
     * Spring Security가 인증 시 자동으로 호출합니다.
     * 
     * ⚠️ 주의: username 파라미터는 실제로 email입니다!
     * - 로그인 시 사용자가 입력하는 식별자
     * - 우리는 email로 로그인하므로 email을 사용
     * 
     * @param username 사용자 식별자 (실제로는 email)
     * @return UserDetails 구현체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 조회 시도: {}", username);

        // email로 사용자 조회
        User user = userRepository.findByEmailAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + username));

        log.debug("사용자 조회 성공: {}", user.getEmail());

        // User 엔티티를 CustomUserDetails로 변환하여 반환
        return new CustomUserDetails(user);
    }

    /**
     * Public ID로 사용자 조회
     * 
     * JWT 검증 시 사용합니다.
     * - JWT의 sub에는 publicId가 저장됨
     * - publicId로 사용자를 찾아 권한 확인
     * 
     * @param publicId 사용자 Public ID (UUID)
     * @return UserDetails 구현체
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByPublicId(String publicId) {
        log.debug("Public ID로 사용자 조회: {}", publicId);

        User user = userRepository.findByPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new UserNotFoundException());

        log.debug("사용자 조회 성공: {}", user.getEmail());

        return new CustomUserDetails(user);
    }

}
