package com.ecommerce.global.security.userdetails;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecommerce.domain.user.entity.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security의 UserDetails 구현체
 * 
 * Spring Security가 인증/인가를 처리할 때 사용하는 사용자 정보입니다.
 * 우리의 User 엔티티를 Spring Security가 이해할 수 있는 형식으로 변환합니다.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user; // User Entity

    /**
     * 사용자의 권한 목록 반환
     * 
     * Spring Security는 권한을 GrantedAuthority 타입으로 관리합니다.
     * 
     * @return 권한 목록 (예: [ROLE_USER], [ROLE_ADMIN])
     */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // user.getRole()은 Role enum (ROLE_USER, ROLE_ADMIN)
        // SimpleGrantedAuthority로 변환하여 반환

        // List<GrantedAuthority> authorities = new ArrayList<>();

        // // 기본 권한
        // authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

        // // 추가 권한
        // if (user.isPremium()) {
        // authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM"));
        // }

        // if (user.isSeller()) {
        // authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        // }

        // return authorities;
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name()));

    }

    /**
     * 비밀번호 반환
     * 
     * Spring Security가 비밀번호 검증 시 사용합니다.
     * (로그인 시에만 사용, 이후에는 null 가능)
     * 
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자 식별자 (username) 반환
     * 
     * Spring Security는 "username"이라는 용어를 사용하지만,
     * 실제로는 사용자를 식별하는 고유값입니다.
     * 
     * 중요: publicId를 반환! (Long id가 아님!)
     * - JWT에 저장되는 값
     * - 외부에 노출되는 식별자
     * 
     * @return 사용자 Public ID (UUID)
     */
    @Override
    public String getUsername() {
        return user.getPublicId(); // 중요!
    }

    /**
     * 계정 만료 여부
     * 
     * true: 계정이 만료되지 않음 (정상)
     * false: 계정이 만료됨 (로그인 불가)
     * 
     * 현재는 항상 true (만료 기능 사용 안 함)
     * 
     * @return true (만료 안 됨)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부
     * 
     * true: 계정이 잠기지 않음 (정상)
     * false: 계정이 잠김 (로그인 불가)
     * 
     * 현재는 항상 true (잠금 기능 사용 안 함)
     * 나중에 user.isLocked() 등으로 변경 가능
     * 
     * @return true (잠김 아님)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호 만료 여부
     * 
     * true: 비밀번호가 만료되지 않음 (정상)
     * false: 비밀번호가 만료됨 (비밀번호 변경 필요)
     * 
     * 현재는 항상 true (만료 기능 사용 안 함)
     * 
     * @return true (만료 안 됨)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부
     * 
     * true: 계정이 활성화됨 (로그인 가능)
     * false: 계정이 비활성화됨 (로그인 불가)
     * 
     * Soft Delete를 고려:
     * - deleted가 false면 활성화
     * - deleted가 true면 비활성화
     * 
     * @return 활성화 여부
     */
    @Override
    public boolean isEnabled() {
        // deleted가 false면 활성화 (true 반환)
        // deleted가 true면 비활성화 (false 반환)
        return !user.getDeleted();
    }

}
