package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @CreatedDate, @LastModifiedDate가 자동으로 작동합니다!
@Configuration
@EnableJpaAuditing  // JPA Auditing 활성화
public class JpaConfig {
}