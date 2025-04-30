package com.eouil.bank.bankapi.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "0123456789abcdef0123456789abcdef"; // HS256, 최소 32자(256bits) 이상
                                                                                // 배포 시 환경변수로 관리해야 함
    private static final long ACCESS_EXP = 1000 * 60 * 30;  // 30분
    private static final long REFRESH_EXP = 1000 * 60 * 60 * 24 * 7; // 7일

    
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // access token
    public static String generateAccessToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // refresh token
    public static String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰을 검증하고 userId 추출
    public static String validateTokenAndGetUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .setAllowedClockSkewSeconds(60) // ← 1분 정도 시간 차 허용
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();

        return claims.getSubject();
    }
    
}