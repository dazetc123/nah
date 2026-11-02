package com.example.betong.Security;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Danh sách thu hồi trong bộ nhớ cho JWT stateless.
 * Token tự hết hạn sẽ được dọn khi có request mới; không lưu plaintext token.
 */
@Service
public class TokenRevocationService {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String token, Claims claims) {
        revokedTokens.put(fingerprint(token), claims.getExpiration().toInstant());
        removeExpired();
    }

    public boolean isRevoked(String token) {
        removeExpired();
        Instant expiresAt = revokedTokens.get(fingerprint(token));
        return expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    private void removeExpired() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }

    private String fingerprint(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Không hỗ trợ SHA-256", ex);
        }
    }
}
