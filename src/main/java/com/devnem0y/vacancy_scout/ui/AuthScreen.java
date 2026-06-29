package com.devnem0y.vacancy_scout.ui;

import com.devnem0y.vacancy_scout.auth.OAuthService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Route("")
public class AuthScreen extends VerticalLayout {

    private final OAuthService oauthService;

    public AuthScreen(OAuthService oauthService) {
        this.oauthService = oauthService;
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Vacancy Scout");
        title.getElement().getStyle().set("text-align", "center");

        Button loginButton = new Button("Войти через hh.ru", event -> {
            String state = oauthService.generateState();
            String codeVerifier = oauthService.generateCodeVerifier();
            String codeChallenge = oauthService.generateCodeChallenge(codeVerifier);

            // Достаём настоящую HttpSession и кладём туда code_verifier
            var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;

            HttpSession httpSession = attributes.getRequest().getSession();
            httpSession.setAttribute("oauth_code_verifier_" + state, codeVerifier);

            String authUrl = oauthService.getAuthorizationUrl(state, codeChallenge);
            getUI().ifPresent(ui -> ui.getPage().setLocation(authUrl));
        });
        loginButton.addClassName("login-button");

        add(title, loginButton);
    }
}
