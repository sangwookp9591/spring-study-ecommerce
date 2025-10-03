package com.ecommerce.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰 생성 및 검증
 * 
 * 대기업 수준의 보안 구현:
 * - Access Token (짧은 만료 시간)
 * - Refresh Token (긴 만료 시간, Redis 저장)
 * - 토큰 자동 갱신
 * - 블랙리스트 (로그아웃)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * SecretKey 생성 (HS256)
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰 생성 (Access + Refresh)
     * 
     * @param authentication 인증 정보
     * @return JwtTokenDto
     */
    public JwtTokenDto generateToken(Authentication authentication) {
        // 권한 정보 추출
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = System.currentTimeMillis();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + jwtProperties.getAccessTokenValidity());
        String accessToken = Jwts.builder().subject(authentication.getName())// 사용자 ID
                .claim(AUTHORITIES_KEY, authorities) // 권한
                .expiration(accessTokenExpiresIn)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .subject(authentication.getName())
                .expiration(new Date(now + jwtProperties.getRefreshTokenValidity()))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

        // Refresh Token을 Redis에 저장
        redisTemplate.opsForValue().set(
                "RT:" + authentication.getName(), // Key: "RT:userId"
                refreshToken,
                jwtProperties.getRefreshTokenValidity(),
                TimeUnit.MILLISECONDS);
        log.info("JWT 토큰 생성 완료: userId={}", authentication.getName());

        return JwtTokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessToeknExpiresIn(accessTokenExpiresIn.getTime())
                .build();
    }

    /**
     * JWT 토큰에서 인증 정보 추출
     * 
     * @param accessToken Access Token
     * @return Authentication
     */
    public Authentication getAuthentication(String accessToken) {
        // 토큰 파싱
        Claims claims = parseClaims(accessToken);

        // 권한 정보가 없으면 예외
        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // UserDetails 생성
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰 유효성 검증
     * 
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            // 블랙리스트 체크 (로그아웃된 토큰)
            if (isTokenBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰입니다");
                return false;
            }

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다", e);
        }

        return false;
    }

    /**
     * 토큰에서 Claims 추출
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Refresh Token으로 Access Token 재발급
     * 
     * @param refreshToken Refresh Token
     * @return 새로운 Access Token
     */
    public String refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // Refresh Token에서 사용자 정보 추출
        Claims claims = parseClaims(refreshToken);
        String userId = claims.getSubject();

        // Redis에 저장된 Refresh Token과 비교
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + userId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh Token이 일치하지 않습니다.");
        }

        // 새로운 Access Token 생성
        long now = System.currentTimeMillis();
        Date accessTokenExpiresIn = new Date(now + jwtProperties.getAccessTokenValidity());

        String newAccessToken = Jwts.builder()
                .subject(userId)
                .claim(AUTHORITIES_KEY, claims.get(AUTHORITIES_KEY))
                .expiration(accessTokenExpiresIn)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
        log.info("Access Token 재발급 완료: userId={}", userId);

        return newAccessToken;
    }

    /**
     * 로그아웃 (토큰 무효화)
     * 
     * @param accessToken Access Token
     */
    public void logout(String accessToken) {
        // 토큰에서 사용자 정보 추출
        Claims claims = parseClaims(accessToken);
        String userId = claims.getSubject();

        // Redis에서 Refresh Token 삭제
        redisTemplate.delete("RT:" + userId);

        // Access Token을 블랙리스트에 추가 (남은 유효 시간 동안)
        long expiration = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                    "BL:" + accessToken, // Key: "BL:token"
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS);
        }

        log.info("로그아웃 완료: userId={}", userId);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("BL:" + token));
    }

    /**
     * 토큰의 남은 유효 시간 조회 (ms)
     */
    public long getTokenExpirationTime(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

}
