package com.ssasinsa.wearagain.domain.auth.infrastructure.client;

import com.ssasinsa.wearagain.domain.auth.config.KakaoOAuthProperties;
import com.ssasinsa.wearagain.domain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.domain.auth.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class KakaoOAuthClient {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final Duration JWKS_CACHE_TTL = Duration.ofMinutes(15);
    private static final long EXPIRATION_CLOCK_SKEW_SECONDS = 60; // Aligns with Kakao OIDC doc tolerance for token expiry validation
    private static final long AUTH_TIME_FUTURE_TOLERANCE_MINUTES = 5; // Grace window for client clock skew on auth_time

    private final WebClient webClient;
    private final KakaoOAuthProperties properties;
    private final Map<String, CachedKey> publicKeyCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    public KakaoOAuthClient(WebClient.Builder webClientBuilder, KakaoOAuthProperties properties) {
        this.webClient = webClientBuilder.build();
        this.properties = properties;
    }

    public KakaoOAuthTokenResponse requestToken(String authorizationCode) {
        try {
            KakaoOAuthTokenResponse response = webClient.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", GRANT_TYPE_AUTHORIZATION_CODE)
                            .with("client_id", properties.clientId())
                            .with("client_secret", properties.clientSecret())
                            .with("redirect_uri", properties.redirectUri())
                            .with("code", authorizationCode))
                    .retrieve()
                    .bodyToMono(KakaoOAuthTokenResponse.class)
                    .doOnError(error -> log.error("Failed to request Kakao token: {}", error.getMessage()))
                    .onErrorMap(error -> new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED, error))
                    .block();
            if (response == null || response.accessToken() == null) {
                throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
            }
            return response;
        } catch (AuthException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED, exception);
        }
    }

    public KakaoUserInfoResponse fetchUserInfo(String accessToken) {
        try {
            KakaoUserInfoResponse response = webClient.get()
                    .uri(properties.userInfoUri())
                    .headers(headers -> {
                        headers.setBearerAuth(accessToken);
                        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                    })
                    .retrieve()
                    .bodyToMono(KakaoUserInfoResponse.class)
                    .block();
            if (response == null) {
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
            }
            return response;
        } catch (WebClientResponseException | WebClientRequestException exception) {
            log.error("Failed to fetch Kakao user info: {}", exception.getMessage());
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED, exception);
        }
    }

    public KakaoIdTokenPayload parseIdToken(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
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
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseClaimsJws(idToken);

            Claims claims = jws.getBody();
            JwsHeader header = jws.getHeader();
            validateAlgorithm(header);
            validateAudience(claims, properties.appClientId(), properties.clientId());
            String providerUserId = claims.getSubject();
            if (!StringUtils.hasText(providerUserId)) {
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
            }
            String email = claims.get("email", String.class);
            String nickname = claims.get("nickname", String.class);
            String profileImageUrl = claims.get("picture", String.class);
            Instant issuedAt = extractInstant(claims.getIssuedAt(), "iat");
            Instant expiresAt = extractInstant(claims.getExpiration(), "exp");
            Instant authTime = extractInstant(claims.get("auth_time"), "auth_time");
            if (issuedAt == null || expiresAt == null) {
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
            }
            if (expiresAt.isBefore(issuedAt)) {
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
            }
            Instant now = Instant.now();
            if (expiresAt.isBefore(now.minusSeconds(EXPIRATION_CLOCK_SKEW_SECONDS))) {
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
            }
            if (authTime != null && authTime.isAfter(now.plus(AUTH_TIME_FUTURE_TOLERANCE_MINUTES, ChronoUnit.MINUTES))) {
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
            }

            String nonce = claims.get("nonce", String.class);

            return new KakaoIdTokenPayload(
                    providerUserId,
                    email,
                    nickname,
                    profileImageUrl,
                    issuedAt,
                    expiresAt,
                    authTime,
                    nonce
            );
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED, exception);
        }
    }

    private void validateAudience(Claims claims, String appClientId, String webClientId) {
        List<String> expectedAudiences = new ArrayList<>();
        if (StringUtils.hasText(appClientId)) {
            expectedAudiences.add(appClientId.trim());
        }
        if (StringUtils.hasText(webClientId)) {
            expectedAudiences.add(webClientId.trim());
        }
        if (expectedAudiences.isEmpty()) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        }
        Object audienceClaim = claims.get("aud");
        if (audienceClaim == null) {
            audienceClaim = claims.getAudience();
        }
        List<String> audiences = extractAudienceList(audienceClaim);
        boolean matched = audiences.stream()
                .anyMatch(aud -> expectedAudiences.stream().anyMatch(expected -> expected.equals(aud)));
        if (!matched) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        }
    }

    private List<String> extractAudienceList(Object audienceClaim) {
        if (audienceClaim == null) {
            return List.of();
        }
        if (audienceClaim instanceof String stringAudience) {
            String normalized = normalizeAudienceValue(stringAudience);
            if (!StringUtils.hasText(normalized)) {
                return List.of();
            }
            return List.of(normalized);
        }
        if (audienceClaim instanceof Iterable<?> iterable) {
            List<String> result = new ArrayList<>();
            for (Object value : iterable) {
                String normalized = normalizeAudienceValue(String.valueOf(value));
                if (StringUtils.hasText(normalized)) {
                    result.add(normalized);
                }
            }
            return result;
        }
        if (audienceClaim.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(audienceClaim);
            List<String> result = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                Object value = java.lang.reflect.Array.get(audienceClaim, index);
                String normalized = normalizeAudienceValue(String.valueOf(value));
                if (StringUtils.hasText(normalized)) {
                    result.add(normalized);
                }
            }
            return result;
        }
        String normalized = normalizeAudienceValue(audienceClaim.toString());
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        return List.of(normalized);
    }

    private Instant extractInstant(Object value, String claimName) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date dateValue) {
            return dateValue.toInstant();
        }
        if (value instanceof Number numberValue) {
            long epochSeconds = numberValue.longValue();
            return Instant.ofEpochSecond(epochSeconds);
        }
        if (value instanceof String stringValue) {
            if (!StringUtils.hasText(stringValue)) {
                return null;
            }
            try {
                long epochSeconds = Long.parseLong(stringValue);
                return Instant.ofEpochSecond(epochSeconds);
            } catch (NumberFormatException exception) {
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED, exception);
            }
        }
        throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
    }

    private void validateAlgorithm(JwsHeader header) {
        if (header == null) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        }
        String algorithm = header.getAlgorithm();
        if (!"RS256".equalsIgnoreCase(algorithm)) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        }
    }

    private String normalizeAudienceValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        if (normalized.startsWith("\"") && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private PublicKey resolvePublicKey(String keyId) {
        if (!StringUtils.hasText(keyId)) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
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
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
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
        KakaoPublicKeysResponse response = fetchPublicKeys();
        if (response == null || response.keys() == null) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        }
        Instant now = Instant.now();
        Map<String, CachedKey> refreshed = new ConcurrentHashMap<>();
        for (KakaoPublicKey key : response.keys()) {
            PublicKey publicKey = createPublicKey(key);
            refreshed.put(key.kid(), new CachedKey(publicKey, now.plus(JWKS_CACHE_TTL)));
        }
        synchronized (publicKeyCache) {
            publicKeyCache.clear();
            publicKeyCache.putAll(refreshed);
        }
    }

    private KakaoPublicKeysResponse fetchPublicKeys() {
        try {
            return webClient.get()
                    .uri(properties.keyUri())
                    .retrieve()
                    .bodyToMono(KakaoPublicKeysResponse.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException exception) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED, exception);
        }
    }

    private PublicKey createPublicKey(KakaoPublicKey key) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(key.n());
            byte[] exponentBytes = Base64.getUrlDecoder().decode(key.e());
            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(key.kty());
            return keyFactory.generatePublic(spec);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED, exception);
        }
    }

    private record CachedKey(PublicKey publicKey, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    public record KakaoIdTokenPayload(
            String providerUserId,
            String email,
            String nickname,
            String profileImageUrl,
            Instant issuedAt,
            Instant expiresAt,
            Instant authenticatedAt,
            String nonce
    ) {
    }

    private record KakaoPublicKeysResponse(List<KakaoPublicKey> keys) {
    }

    private record KakaoPublicKey(
            String kid,
            String kty,
            String alg,
            String use,
            String n,
            String e
    ) {
    }
}
