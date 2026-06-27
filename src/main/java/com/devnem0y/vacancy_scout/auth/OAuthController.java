package com.devnem0y.vacancy_scout.auth;

import com.devnem0y.vacancy_scout.users.UserPreferencesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/auth")
public class OAuthController {

    private final OAuthService oauthService;
    private final UserPreferencesService userPreferencesService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${HH_CLIENT_ID}")
    private String clientId;

    @Value("${HH_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${HH_REDIRECT_URI}")
    private String redirectUri;

    public OAuthController(
            OAuthService oauthService,
            UserPreferencesService userPreferencesService
    ) {
        this.oauthService = oauthService;
        this.userPreferencesService = userPreferencesService;
    }

    @GetMapping("/hh")
    public String startAuth(HttpSession session) {
        String state = oauthService.generateState();
        String codeVerifier = oauthService.generateCodeVerifier();
        String codeChallenge = oauthService.generateCodeChallenge(codeVerifier);

        // ВАЖНО: тут ты должен положить в ту же сессию, из которой потом будешь доставать.
        // Сейчас у тебя в UI — VaadinSession, в контроллере — HttpSession. Это разные сессии.
        session.setAttribute("oauth_code_verifier_" + state, codeVerifier);

        String redirectUrl = oauthService.getAuthorizationUrl(state, codeChallenge);
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/callback")
    public String handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session
            // Model model <-- УДАЛИТЬ
    ) {
        java.util.logging.Logger log = java.util.logging.Logger.getLogger("OAuthController");
        log.info("Callback entered. state=" + state + ", code.length=" + code.length());

        String codeVerifier = (String) session.getAttribute("oauth_code_verifier_" + state);
        if (codeVerifier == null) {
            log.warning("Code verifier not found in session for state: " + state);
            // Не возвращаем шаблон, делаем редирект.
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
                log.warning("No access_token in response");
                return "redirect:/?error=no_token";
            }

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            org.springframework.http.HttpEntity<String> requestEntity = new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<Map> meResponse = restTemplate.exchange(
                    "https://api.hh.ru/me",
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> meData = meResponse.getBody();
            String hhUserId = (String) meData.get("id");

            if (hhUserId == null) {
                log.warning("No id in /me response");
                return "redirect:/?error=no_user_id";
            }

            userPreferencesService.findOrCreate(hhUserId);

            log.info("OAuth flow succeeded for hhUserId=" + hhUserId);
            return "redirect:/";

        } catch (Exception e) {
            e.printStackTrace();
            log.severe("OAuth flow failed: " + e.getMessage());
            return "redirect:/?error=oauth_failed";
        }
    }
}
