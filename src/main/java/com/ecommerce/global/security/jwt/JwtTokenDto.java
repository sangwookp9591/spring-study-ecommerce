package com.ecommerce.global.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * JWT 토큰 DTO
 * 
 * Access Token + Refresh Token 함께 반환
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenDto {
    private String grantType; // "Bearer"
    private String accessToken; // "Access Token (30분)"
    private String refreshToken; // "Refresh Token (14일)"
    private Long accessToeknExpiresIn; // Access Token 만료
}
