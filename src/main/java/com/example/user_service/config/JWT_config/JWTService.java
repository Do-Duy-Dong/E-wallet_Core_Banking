package com.example.user_service.config.JWT_config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JWTService {
    @Value("${jwt.accessSecret}")
    private String accessKey;
    @Value("${jwt.refreshSecret}")
    private String refreshKey;
    @Value("${jwt.accessExpirationMs}")
    private Long accessExpirationMs;
    @Value("${jwt.refreshExpirationMs}")
    private Long refreshExpirationMs;
    public String generateToken(String key,String userName,Long expiration){
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(HashKey(key), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key HashKey(String key) {
        byte[] keyByte= Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyByte);
    }

    public String generateAccessToken(String userName){
        return generateToken(accessKey,userName,accessExpirationMs);
    }
    public String generateRefreshToken(String userName){
        return generateToken(refreshKey,userName,refreshExpirationMs);
    }
    public Claims validateToken(String token,String key){
        return Jwts.parserBuilder()
                .setSigningKey(HashKey(key))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public Claims validateAccessToken(String token){
        return validateToken(token,accessKey);
    }
    public Claims validateRefreshToken(String token) {
        return validateToken(token, refreshKey);
    }

    public String extractUsername(String token) {
        try {
            Claims claims = validateRefreshToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = validateRefreshToken(token);
            return claims.getSubject().equals(username) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
