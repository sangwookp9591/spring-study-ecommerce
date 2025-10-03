package com.ecommerce.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/* 
 * JWT 설정 Properties
 * 
 * application.yml의 jwt.* 값을 자동으로 매핑
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;              //비밀키
    private Long accessTokenValidity;   // Access Token 유효 시간 (ms)
    private Long refreshTokenValidity;  // Refresh Token 유효 시간 (ms)
    
}
