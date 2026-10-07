package com.example.betong.Security;

import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class GoogleOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final String loginUrl;

    public GoogleOAuth2FailureHandler(
            @Value("${app.oauth2.login-url:http://localhost:3000/login}") String loginUrl) {
        this.loginUrl = loginUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        response.sendRedirect(loginUrl);
    }
}
