package com.ssasinsa.wearagain.domain.auth.infrastructure.jwt;

import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.domain.auth.config.AdminJwtProperties;
import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class AdminJwtTokenProvider {

    private final AdminJwtProperties properties;
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;

    public AdminJwtTokenProvider(AdminJwtProperties properties) {
        this.properties = properties;
        this.accessTokenKey = createKey(properties.accessToken().secret());
        this.refreshTokenKey = createKey(properties.refreshToken().secret());
    }

    public JwtToken createAccessToken(AdminUser admin) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(properties.accessToken().validity());
        String token = Jwts.builder()
                .issuer(properties.issuer())
                .subject(admin.getId().toString())
                .claim("email", admin.getEmail())
                .claim("name", admin.getName())
                .claim("role", admin.getRole().name())
                .claim("mustChangePassword", admin.isMustChangePassword())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(accessTokenKey, SignatureAlgorithm.HS256)
                .compact();
        return new JwtToken(token, expiresAt, null);
    }

    public JwtToken createRefreshToken(AdminUser admin) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(properties.refreshToken().validity());
        UUID tokenId = UUID.randomUUID();
        String token = Jwts.builder()
                .issuer(properties.issuer())
                .subject(admin.getId().toString())
                .claim("tokenId", tokenId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(refreshTokenKey, SignatureAlgorithm.HS256)
                .compact();
        return new JwtToken(token, expiresAt, tokenId);
    }

    public AccessTokenClaims parseAccessToken(String token) {
        Claims claims = parseClaims(token, accessTokenKey);
        Long adminId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        String roleValue = claims.get("role", String.class);
        Boolean mustChangePassword = claims.get("mustChangePassword", Boolean.class);
        AdminRole role = roleValue != null ? AdminRole.valueOf(roleValue) : AdminRole.MANAGER;
        return new AccessTokenClaims(adminId, email, name, role, Boolean.TRUE.equals(mustChangePassword));
    }

    public RefreshTokenClaims parseRefreshToken(String token) {
        Claims claims = parseClaims(token, refreshTokenKey);
        Long adminId = Long.parseLong(claims.getSubject());
        String tokenIdValue = claims.get("tokenId", String.class);
        if (tokenIdValue == null) {
            throw new JwtException("tokenId claim missing");
        }
        UUID tokenId = UUID.fromString(tokenIdValue);
        Instant issuedAt = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null;
        Instant expiresAt = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;
        return new RefreshTokenClaims(adminId, tokenId, issuedAt, expiresAt);
    }

    private Claims parseClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey createKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public record AccessTokenClaims(
            Long adminId,
            String email,
            String name,
            AdminRole role,
            boolean mustChangePassword
    ) {
    }

    public record RefreshTokenClaims(
            Long adminId,
            UUID tokenId,
            Instant issuedAt,
            Instant expiresAt
    ) {
    }
}
