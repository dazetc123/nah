package com.example.betong.Security;

import com.example.betong.DTO.response.auth.LoginResponse;
import com.example.betong.Exception.AuthException;
import com.example.betong.Service.auth.AuthService;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final String loginUrl;

    public GoogleOAuth2SuccessHandler(
            AuthService authService,
            @Value("${app.oauth2.login-url:http://localhost:3000/login}") String loginUrl) {
        this.authService = authService;
        this.loginUrl = loginUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)
                    || !"google".equals(oauth2Authentication.getAuthorizedClientRegistrationId())) {
                throw new AuthException("Nhà cung cấp OAuth2 không được hỗ trợ", HttpStatus.UNAUTHORIZED);
            }
            LoginResponse loginResponse = authService.dangNhapBangGoogle(oauth2Authentication.getPrincipal());
            Cookie accessTokenCookie = new Cookie("BETONG_ACCESS_TOKEN", loginResponse.getAccessToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(86400);
            response.addCookie(accessTokenCookie);
            String redirectUrl = loginUrl
                    + (loginUrl.contains("#") ? "&" : "#")
                    + "oauth2=success"
                    + "&accessToken=" + encode(loginResponse.getAccessToken())
                    + "&tokenType=" + encode(loginResponse.getTokenType())
                    + "&idTK=" + loginResponse.getIdTK()
                    + "&hoTen=" + encode(loginResponse.getHoTen())
                    + "&tenVaiTro=" + encode(loginResponse.getTenVaiTro())
                    + "&phaiDoiMatKhau=" + loginResponse.isPhaiDoiMatKhau();
            response.sendRedirect(redirectUrl);
        } catch (AuthException exception) {
            response.sendError(exception.getStatus().value(), exception.getMessage());
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
