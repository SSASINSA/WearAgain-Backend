package com.ssasinsa.wearagain.auth.infrastructure.jwt;

import com.ssasinsa.wearagain.auth.config.JwtProperties;
import com.ssasinsa.wearagain.auth.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final Key accessTokenKey;
    private final Key refreshTokenKey;

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

    private Key createKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
