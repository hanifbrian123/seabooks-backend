package com.fishcatch.backend.security;

import com.fishcatch.backend.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    @Value("${app.jwt.secret:fishcatch-default-secret-key-2026-neon-postgresql-fleet-app}")
    private String jwtSecret;

    private static final long EXPIRATION_SECONDS = 7 * 24 * 3600L; // 7 days

    public String generateToken(User user) {
        return generateToken(user, EXPIRATION_SECONDS);
    }

    public String generateToken(User user, long durationSeconds) {
        long exp = Instant.now().getEpochSecond() + durationSeconds;
        String payload = user.getId() + "|" + exp + "|" + user.getEmail();
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);
        return encodedPayload + "." + signature;
    }

    public Long validateAndExtractUserId(String token) {
        if (token == null || !token.contains(".")) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return null;
        }
        String encodedPayload = parts[0];
        String providedSignature = parts[1];
        String expectedSignature = sign(encodedPayload);

        if (!MessageDigest.isEqual(
                providedSignature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
        )) {
            return null;
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
            String[] fields = payload.split("\\|", 3);
            if (fields.length < 2) {
                return null;
            }
            long userId = Long.parseLong(fields[0]);
            long exp = Long.parseLong(fields[1]);
            if (Instant.now().getEpochSecond() > exp) {
                return null;
            }
            return userId;
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign token", e);
        }
    }
}

