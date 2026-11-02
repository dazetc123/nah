package com.example.betong.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientKeyFilter extends OncePerRequestFilter {
    private final Set<String> acceptedClientKeys;

    public ClientKeyFilter(
            @Value("${app.client-key}") String webClientKey,
            @Value("${app.mobile-client-key}") String mobileClientKey) {
        this.acceptedClientKeys = Arrays.stream(new String[]{webClientKey, mobileClientKey})
                .map(String::trim)
                .filter(key -> !key.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientKey = request.getHeader("ClientKey");
        if (clientKey == null || !acceptedClientKeys.contains(clientKey.trim())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"ClientKey không hợp lệ\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith(request.getContextPath() + "/api/")
                || path.equals(request.getContextPath() + "/api/auth/dang-nhap-google");
    }
}
