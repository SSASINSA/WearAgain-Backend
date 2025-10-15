package com.ssasinsa.wearagain.auth.infrastructure.client;

import com.ssasinsa.wearagain.auth.config.AppleOAuthProperties;
import com.ssasinsa.wearagain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.auth.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolverAdapter;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AppleOAuthClient {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final Duration CLIENT_SECRET_VALIDITY = Duration.ofMinutes(5);
    private static final Duration JWKS_CACHE_TTL = Duration.ofMinutes(15);
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    private final WebClient webClient;
    private final AppleOAuthProperties properties;
    private final Map<String, CachedKey> publicKeyCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    public AppleOAuthClient(WebClient.Builder webClientBuilder, AppleOAuthProperties properties) {
        this.webClient = webClientBuilder.build();
        this.properties = properties;
    }

    public AppleOAuthTokenResponse requestToken(String authorizationCode) {
        try {
            AppleOAuthTokenResponse response = webClient.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", GRANT_TYPE_AUTHORIZATION_CODE)
                            .with("client_id", properties.clientId())
                            .with("client_secret", generateClientSecret())
                            .with("code", authorizationCode)
                            .with("redirect_uri", properties.redirectUri())
                    )
                    .retrieve()
                    .bodyToMono(AppleOAuthTokenResponse.class)
                    .block();
            if (response == null) {
                throw new AuthException(AuthErrorCode.APPLE_TOKEN_REQUEST_FAILED);
            }
            return response;
        } catch (WebClientResponseException exception) {
            throw new AuthException(AuthErrorCode.APPLE_TOKEN_REQUEST_FAILED, exception);
        }
    }

    public AppleUserInfo parseIdToken(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED);
        }
        try {
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKeyResolver(new SigningKeyResolverAdapter() {
                        @Override
                        public PublicKey resolveSigningKey(io.jsonwebtoken.JwsHeader header, Claims claims) {
                            String keyId = header.getKeyId();
                            return resolvePublicKey(keyId);
                        }
                    })
                    .requireIssuer(APPLE_ISSUER)
                    .build()
                    .parseClaimsJws(idToken);

            Claims claims = jws.getBody();
            validateAudience(claims);
            String providerUserId = claims.getSubject();
            String email = claims.get("email", String.class);
            boolean emailVerified = parseEmailVerified(claims.get("email_verified"));

            return new AppleUserInfo(providerUserId, email, emailVerified);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED, exception);
        }
    }

    private void validateAudience(Claims claims) {
        Object audienceClaim = claims.get("aud");

        if (audienceClaim instanceof String audience) {
            if (properties.clientId().equals(audience)) {
                return;
            }
        } else if (audienceClaim instanceof Collection<?> audienceCollection) {
            boolean matched = audienceCollection.stream()
                    .map(Object::toString)
                    .anyMatch(properties.clientId()::equals);

            if (matched) {
                return;
            }
        } else if (audienceClaim != null && audienceClaim.getClass().isArray()) {
            int length = Array.getLength(audienceClaim);
            for (int index = 0; index < length; index++) {
                Object value = Array.get(audienceClaim, index);
                if (properties.clientId().equals(String.valueOf(value))) {
                    return;
                }
            }
        }

        throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED);
    }

    private boolean parseEmailVerified(Object claim) {
        if (claim instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (claim instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return false;
    }

    private PublicKey resolvePublicKey(String keyId) {
        if (!StringUtils.hasText(keyId)) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED);
        }

        CachedKey cachedKey = publicKeyCache.get(keyId);
        if (cachedKey != null && !cachedKey.isExpired()) {
            return cachedKey.publicKey();
        }

        ReentrantLock lock = keyLocks.computeIfAbsent(keyId, key -> new ReentrantLock());
        lock.lock();
        try {
            cachedKey = publicKeyCache.get(keyId);
            if (cachedKey != null && !cachedKey.isExpired()) {
                return cachedKey.publicKey();
            }

            refreshPublicKeys();
            CachedKey refreshed = publicKeyCache.get(keyId);
            if (refreshed == null) {
                throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED);
            }
            return refreshed.publicKey();
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                keyLocks.remove(keyId, lock);
            }
        }
    }

    private void refreshPublicKeys() {
        ApplePublicKeysResponse response = fetchPublicKeys();
        if (response == null || response.keys() == null) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED);
        }
        Instant now = Instant.now();
        Map<String, CachedKey> refreshed = new ConcurrentHashMap<>();
        for (ApplePublicKey key : response.keys()) {
            PublicKey publicKey = createPublicKey(key);
            refreshed.put(key.kid(), new CachedKey(publicKey, now.plus(JWKS_CACHE_TTL)));
        }
        publicKeyCache.clear();
        publicKeyCache.putAll(refreshed);
    }

    private ApplePublicKeysResponse fetchPublicKeys() {
        try {
            return webClient.get()
                    .uri(properties.keyUri())
                    .retrieve()
                    .bodyToMono(ApplePublicKeysResponse.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException exception) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED, exception);
        }
    }

    private PublicKey createPublicKey(ApplePublicKey key) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(key.n());
            byte[] exponentBytes = Base64.getUrlDecoder().decode(key.e());
            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.APPLE_USERINFO_REQUEST_FAILED, exception);
        }
    }

    private String generateClientSecret() {
        try {
            PrivateKey privateKey = loadPrivateKey();
            Instant now = Instant.now();
            Instant expiresAt = now.plus(CLIENT_SECRET_VALIDITY);
            JwtBuilder jwt = Jwts.builder()
                    .header()   // Header
                    .add("alg", "ES256")
                    .add("kid", properties.keyId())
                    .and()
                    .issuer(properties.teamId())                          // iss
                    .subject(properties.clientId())                       // sub
                    .audience()                                          // aud 시작
                    .add(APPLE_ISSUER)                // aud 값 추가
                    .and()
                    .issuedAt(Date.from(now))                // iat
                    .expiration(java.util.Date.from(expiresAt))   // exp
                    .signWith(privateKey, SignatureAlgorithm.ES256);
            return jwt.compact();
        } catch (IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.APPLE_TOKEN_REQUEST_FAILED, exception);
        }
    }

    private PrivateKey loadPrivateKey() {
        try {
            String raw = Optional.ofNullable(properties.privateKey())
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> new AuthException(AuthErrorCode.APPLE_TOKEN_REQUEST_FAILED));

            String sanitized = raw
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(sanitized);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC"); // Apple은 P-256 EC 키 사용
            return keyFactory.generatePrivate(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.APPLE_TOKEN_REQUEST_FAILED, exception);
        }
    }

    private record CachedKey(PublicKey publicKey, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private record ApplePublicKeysResponse(List<ApplePublicKey> keys) {
    }

    private record ApplePublicKey(
            String kid,
            String n,
            String e
    ) {
    }

    public record AppleUserInfo(
            String providerUserId,
            String email,
            boolean emailVerified
    ) {
    }
}
