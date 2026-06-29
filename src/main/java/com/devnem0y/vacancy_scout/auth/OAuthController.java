package com.devnem0y.vacancy_scout.auth;

import com.devnem0y.vacancy_scout.users.UserPreferencesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class OAuthController {

    private final OAuthService oauthService;
    private final UserPreferencesService userPreferencesService;
    private final RestTemplate restTemplate;

    @Value("${HH_CLIENT_ID}")
    private String clientId;

    @Value("${HH_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${HH_REDIRECT_URI}")
    private String redirectUri;

    public OAuthController(
            OAuthService oauthService,
            UserPreferencesService userPreferencesService,
            RestTemplate restTemplate) {
        this.oauthService = oauthService;
        this.userPreferencesService = userPreferencesService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/callback")
    public String handleCallback(@RequestParam String code, @RequestParam String state, HttpSession session) {
        String codeVerifier = (String) session.getAttribute("oauth_code_verifier_" + state);

        if (codeVerifier == null) {
            return "redirect:/?error=session_expired";
        }
        session.removeAttribute("oauth_code_verifier_" + state);

        try {
            Map<String, String> tokenParams = new HashMap<>();
            tokenParams.put("grant_type", "authorization_code");
            tokenParams.put("code", code);
            tokenParams.put("client_id", clientId);
            tokenParams.put("client_secret", clientSecret);
            tokenParams.put("redirect_uri", redirectUri);
            tokenParams.put("code_verifier", codeVerifier);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    "https://hh.ru/oauth/token",
                    tokenParams,
                    Map.class
            );

            Map<String, Object> tokenData = tokenResponse.getBody();
            String accessToken = (String) tokenData.get("access_token");

            if (accessToken == null) {
                return "redirect:/?error=no_token";
            }

            // Запрос к /me для получения hhUserId
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            org.springframework.http.HttpEntity<String> requestEntity = new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<Map> meResponse = restTemplate.exchange(
                    "https://api.hh.ru/me",
                    org.springframework.http.HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> meData = meResponse.getBody();
            String hhUserId = (String) meData.get("id");

            if (hhUserId == null) {
                return "redirect:/?error=no_user_id";
            }

            // СОХРАНЕНИЕ В СЕССИЮ (Ключевой момент для будущего VacancyController)
            session.setAttribute("hh_access_token", accessToken);
            session.setAttribute("hh_user_id", hhUserId);

            userPreferencesService.findOrCreate(hhUserId);

            return "redirect:/dashboard";

        } catch (Exception e) {
            return "redirect:/?error=oauth_failed";
        }
    }
}
