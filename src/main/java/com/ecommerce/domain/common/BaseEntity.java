package com.ecommerce.domain.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass  // 상속받는 엔티티에 필드 추가
@EntityListeners(AuditingEntityListener.class)  // 자동으로 시간 기록
public abstract class BaseEntity {
    
    @CreatedDate  // 생성 시간 자동 기록
    @Column(updatable = false)  // 수정 불가
    private LocalDateTime createdAt;
    
    @LastModifiedDate  // 수정 시간 자동 기록
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean deleted = false;  // Soft Delete
    
    private LocalDateTime deletedAt;
    
    // Soft Delete: 실제로 삭제하지 않고 deleted = true로 표시
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }
}