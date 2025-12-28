package com.spareparts.spareparts_backend.utill;

import com.spareparts.spareparts_backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import static io.jsonwebtoken.Jwts.*;

@Component
public class JWTTokenGenerator {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // 1 hour
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // ================= TOKEN GENERATION =================
    public String generateToken(User user) {
        return builder()
                .setId(String.valueOf(user.getUserId()))
                .setSubject(user.getEmail())
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ================= TOKEN VALIDATION =================

    public boolean validateToken(String token) {
        try {
            Claims claims = getAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ================= EXTRACT METHODS =================

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return getAllClaims(token).get("role", String.class);
    }

    public Integer extractUserId(String token) {
        return getAllClaims(token).get("userId", Integer.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ================= PRIVATE HELPERS =================

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)       // 0.12.x uses SecretKey directly
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getAllClaims(token));
    }
}
