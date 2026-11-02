package com.example.betong.Security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Các API vẫn được phép gọi dù tài khoản đang bị buộc đổi mật khẩu. */
    private static final Set<String> DUOC_PHEP_KHI_CHUA_DOI_MK = Set.of(
            "/api/auth/doi-mat-khau-lan-dau",
            "/api/auth/dang-xuat"
    );

    private final JwtUtil jwtUtil;
    private final TokenRevocationService tokenRevocationService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenRevocationService tokenRevocationService) {
        this.jwtUtil = jwtUtil;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "BETONG_ACCESS_TOKEN".equals(cookie.getName()))
                    .map(jakarta.servlet.http.Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (token != null && !token.isBlank()) {
            if (jwtUtil.isTokenValid(token) && !tokenRevocationService.isRevoked(token)) {
                Claims claims = jwtUtil.parseClaims(token);
                String tenDangNhap = claims.getSubject();
                String vaiTro = claims.get("vaiTro", String.class);
                Boolean phaiDoiMatKhau = claims.get("phaiDoiMatKhau", Boolean.class);

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro));
                var authentication = new UsernamePasswordAuthenticationToken(tenDangNhap, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Chặn mọi API khác nếu tài khoản đang bị buộc đổi mật khẩu lần đầu.
                if (Boolean.TRUE.equals(phaiDoiMatKhau) && !DUOC_PHEP_KHI_CHUA_DOI_MK.contains(request.getRequestURI())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"message\":\"Bạn cần đổi mật khẩu trước khi tiếp tục sử dụng hệ thống\"}");
                    return; // dừng lại, không cho đi tiếp vào Controller
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
