package com.ecommerce.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * publicId로 사용자 찾기
     */
    Optional<User> findByPublicIdAndDeletedFalse(String publicId);
    
    /**
     * 이메일로 사용자 찾기
     * 
     * 생성되는 쿼리:
     * SELECT * FROM users WHERE email = ? AND deleted = false
     * 
     * @param email 이메일
     * @return Optional<User>
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

        /**
     * 이메일 존재 여부 확인
     * 
     * 생성되는 쿼리:
     * SELECT EXISTS(SELECT 1 FROM users WHERE email = ? AND deleted = false)
     * 
     * @param email 이메일
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByEmailAndDeletedFalse(String email);

    
} 