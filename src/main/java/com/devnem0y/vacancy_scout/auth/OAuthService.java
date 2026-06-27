package com.devnem0y.vacancy_scout.auth;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.util.Base64;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@Service
public class OAuthService {

    private final String clientId;
    private final String redirectUri;
    private final SecureRandom random = new SecureRandom();

    public OAuthService(
            @Value("${HH_CLIENT_ID}") String clientId,
            @Value("${HH_REDIRECT_URI}") String redirectUri
    ) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    /**
     * Генерирует случайный code_verifier (64 символа) для PKCE.
     */
    public String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        // Base64URL без паддинга — именно такой формат требует HH
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Из code_verifier делает code_challenge (SHA-256 + base64url).
     */
    public String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // В реальности тут лучше логировать ошибку, но SHA-256 есть всегда
            throw new RuntimeException("Failed to generate code challenge", e);
        }
    }

    /**
     * Генерируем случайный state (защита от CSRF).
     * Можно оставить как было, это отдельная защита.
     */
    public String generateState() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Формируем URL для редиректа на HH с полным набором параметров PKCE.
     */
    public String getAuthorizationUrl(String state, String codeChallenge) {
        return String.format(
                "https://hh.ru/oauth/authorize?" +
                        "response_type=code&" +
                        "client_id=%s&" +
                        "redirect_uri=%s&" +
                        "state=%s&" +
                        "code_challenge=%s&" +
                        "code_challenge_method=S256",
                clientId, redirectUri, state, codeChallenge
        );
    }
}

