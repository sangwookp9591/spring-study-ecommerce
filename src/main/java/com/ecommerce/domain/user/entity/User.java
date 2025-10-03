package com.ecommerce.domain.user.entity;

import com.ecommerce.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티
 * 
 * BaseEntity를 상속받아 공통 필드를 자동으로 갖게 됩니다:
 * - createdAt: 회원가입 시간
 * - updatedAt: 프로필 수정 시간
 * - deleted: 탈퇴 여부
 * - deletedAt: 탈퇴 시간
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {  // ← BaseEntity 상속!
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    
    // 비밀번호 변경
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        // updatedAt은 자동으로 현재 시간으로 업데이트됨!
    }
    
    // 이름 변경
    public void updateName(String newName) {
        this.name = newName;
        // updatedAt은 자동으로 현재 시간으로 업데이트됨!
    }
    
    // delete(), restore() 메서드는 BaseEntity에 이미 있음!
}