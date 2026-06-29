package com.devnem0y.vacancy_scout.auth;

import com.devnem0y.vacancy_scout.vacancies.HhVacancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.util.Base64;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@Service
public class OAuthService {

    private static final Logger log = LoggerFactory.getLogger(OAuthService.class);

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

    public String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate code challenge", e);
        }
    }

    public String generateState() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String getAuthorizationUrl(String state, String codeChallenge) {
        var url = String.format(
                "https://hh.ru/oauth/authorize?" +
                        "response_type=code&" +
                        "client_id=%s&" +
                        "redirect_uri=%s&" +
                        "scope=vacancies&" +
                        "state=%s&" +
                        "code_challenge=%s&" +
                        "code_challenge_method=S256",
                clientId, redirectUri, state, codeChallenge
        );
        log.info("[OAuthService] Generated auth URL: {}", url);
        return url;
    }
}
