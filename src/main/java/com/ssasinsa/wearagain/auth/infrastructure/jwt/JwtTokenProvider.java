package com.ssasinsa.wearagain.auth.infrastructure.jwt;

import com.ssasinsa.wearagain.auth.config.JwtProperties;
import com.ssasinsa.wearagain.auth.domain.User;
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
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.accessTokenKey = createKey(jwtProperties.accessToken().secret());
        this.refreshTokenKey = createKey(jwtProperties.refreshToken().secret());
    }

    public JwtToken createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.accessToken().validity());
        String token = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("displayName", user.getDisplayName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(accessTokenKey, SignatureAlgorithm.HS256)
                .compact();
        return new JwtToken(token, expiresAt, null);
    }

    public JwtToken createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.refreshToken().validity());
        UUID tokenId = UUID.randomUUID();
        String token = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getId().toString())
                .claim("tokenId", tokenId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(refreshTokenKey, SignatureAlgorithm.HS256)
                .compact();
        return new JwtToken(token, expiresAt, tokenId);
    }

    private SecretKey createKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public RefreshTokenClaims parseRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshTokenKey)
                    .requireIssuer(jwtProperties.issuer())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            String tokenIdValue = claims.get("tokenId", String.class);
            if (tokenIdValue == null) {
                throw new JwtException("tokenId claim missing");
            }
            UUID tokenId = UUID.fromString(tokenIdValue);
            Instant issuedAt = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null;
            Instant expiresAt = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;
            return new RefreshTokenClaims(userId, tokenId, issuedAt, expiresAt);
        } catch (JwtException | IllegalArgumentException exception) {
            throw exception;
        }
    }

    public record RefreshTokenClaims(
            UUID userId,
            UUID tokenId,
            Instant issuedAt,
            Instant expiresAt
    ) {
    }
}
