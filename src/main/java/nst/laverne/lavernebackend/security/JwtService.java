package nst.laverne.lavernebackend.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final ObjectMapper objectMapper;
    private final String secretKey;
    private final long expirationMs;

    public JwtService(@Value("${security.jwt.secret-key}") String secretKey,
                      @Value("${security.jwt.expiration}") long expirationMs) {
        this.objectMapper = new ObjectMapper();
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, String role) {
        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encodeJson(Map.of(
                    "sub", username,
                    "role", role,
                    "exp", Instant.now().plusMillis(expirationMs).getEpochSecond()
            ));
            String unsigned = header + "." + payload;
            return unsigned + "." + sign(unsigned);
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de generer le token", exception);
        }
    }

    public TokenPrincipal validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!sign(unsigned).equals(parts[2])) {
                return null;
            }
            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            long expiration = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > expiration) {
                return null;
            }
            return new TokenPrincipal(String.valueOf(payload.get("sub")), String.valueOf(payload.get("role")));
        } catch (Exception exception) {
            return null;
        }
    }

    private String encodeJson(Map<String, ?> value) throws Exception {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
