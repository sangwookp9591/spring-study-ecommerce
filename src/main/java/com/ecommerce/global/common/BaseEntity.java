package com.ecommerce.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 기본 클래스
 * 
 * 공통 필드:
 * - createdAt: 생성 시간 (자동 기록)
 * - updatedAt: 수정 시간 (자동 기록)
 * - deleted: 삭제 여부 (Soft Delete)
 * - deletedAt: 삭제 시간
 * 
 * 사용 방법:
 * @Entity
 * public class User extends BaseEntity {
 *     // User만의 필드들...
 * }
 */
@Getter
@MappedSuperclass  // ← 이게 핵심!(공통 매핑 정보만 제공하는 부모 클래스를 만들 때 사용하는 애노테이션)
@EntityListeners(AuditingEntityListener.class)  //엔티티의 생성/수정 시점(Auditing) 을 자동으로 관리
public abstract class BaseEntity {
    
    /**
     * 생성 시간
     * 
     * - 엔티티가 처음 저장될 때 자동으로 현재 시간이 기록됩니다
     * - 한 번 기록되면 수정되지 않습니다 (updatable = false)
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     * 
     * - 엔티티가 수정될 때마다 자동으로 현재 시간으로 업데이트됩니다
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 삭제 여부 (Soft Delete)
     * 
     * - true: 삭제됨
     * - false: 삭제 안 됨 (기본값)
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    /**
     * 삭제 시간
     * 
     * - delete() 메서드 호출 시 현재 시간이 기록됩니다
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * 삭제 처리 (Soft Delete)
     * 
     * 실제로 DB에서 삭제되지 않고, deleted 플래그만 true로 변경됩니다.
     * 
     * 사용 예시:
     * User user = userRepository.findById(1L).orElseThrow();
     * user.delete();  // deleted = true, deletedAt = 현재시간
     * userRepository.save(user);
     */
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * 삭제 취소 (복구)
     * 
     * deleted 플래그를 false로 되돌립니다.
     * 
     * 사용 예시:
     * User user = userRepository.findById(1L).orElseThrow();
     * user.restore();  // deleted = false, deletedAt = null
     * userRepository.save(user);
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }
}