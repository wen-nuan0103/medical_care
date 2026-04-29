package com.xuenai.medical.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expireHours;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${medical-care.auth.jwt-secret}") String secret,
                      @Value("${medical-care.auth.token-expire-hours}") long expireHours) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expireHours = expireHours;
    }

    public String createToken(CurrentUser user) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("uid", user.id());
            payload.put("username", user.username());
            payload.put("realName", user.realName());
            payload.put("roles", user.roles());
            payload.put("exp", Instant.now().plusSeconds(expireHours * 3600).getEpochSecond());

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signingInput = encodedHeader + "." + encodedPayload;
            return signingInput + "." + sign(signingInput);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Token 生成失败");
        }
    }

    public CurrentUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 格式错误");
            }
            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = sign(signingInput);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 签名无效");
            }

            Map<String, Object> payload = objectMapper.readValue(
                    URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 已过期");
            }

            Long userId = ((Number) payload.get("uid")).longValue();
            String username = String.valueOf(payload.get("username"));
            String realName = String.valueOf(payload.get("realName"));
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) payload.get("roles");
            return new CurrentUser(userId, username, realName, roles);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 解析失败");
        }
    }

    private String encodeJson(Object value) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

