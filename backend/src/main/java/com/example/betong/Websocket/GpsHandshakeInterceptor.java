package com.example.betong.Websocket;

import com.example.betong.Security.JwtUtil;
import com.example.betong.Security.TokenRevocationService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * Xác thực JWT ngay lúc "bắt tay" (handshake) WebSocket.
 *
 * Lý do phải xác thực ở đây thay vì để JwtAuthenticationFilter xử lý như API
 * thường: sau khi kết nối WebSocket được nâng cấp (upgrade) thành công,
 * request HTTP ban đầu kết thúc và SecurityContextHolder gắn với nó cũng mất
 * theo — nếu không lưu lại thông tin tài khoản vào session attributes ngay
 * lúc này, Handler sẽ không còn biết ai đang gửi dữ liệu lên.
 *
 * Việc .requestMatchers("/ws/**").hasRole("Tài xế") ở SecurityConfig đã chặn
 * request chưa đăng nhập/không đúng vai trò từ trước khi tới đây; lớp kiểm
 * tra này là tầng phòng vệ thứ 2 (giống cách ClientKeyFilter + JwtAuthenticationFilter
 * đang được dùng song song cho các API khác trong dự án).
 */
@Component
public class GpsHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final TokenRevocationService tokenRevocationService;

    public GpsHandshakeInterceptor(JwtUtil jwtUtil, TokenRevocationService tokenRevocationService) {
        this.jwtUtil = jwtUtil;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = layToken(request);
        if (token == null || token.isBlank()
                || !jwtUtil.isTokenValid(token) || tokenRevocationService.isRevoked(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Claims claims = jwtUtil.parseClaims(token);
        String vaiTro = claims.get("vaiTro", String.class);
        if (!"Tài xế".equals(vaiTro)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        attributes.put("tenDangNhap", claims.getSubject());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // Không cần xử lý thêm sau khi bắt tay thành công.
    }

    /** Ưu tiên header Authorization (giống API thường); dự phòng query param ?token=... */
    private String layToken(ServerHttpRequest request) {
        List<String> header = request.getHeaders().get("Authorization");
        if (header != null && !header.isEmpty() && header.get(0).startsWith("Bearer ")) {
            return header.get(0).substring(7);
        }
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                int idx = param.indexOf('=');
                if (idx > 0 && "token".equals(param.substring(0, idx))) {
                    return param.substring(idx + 1);
                }
            }
        }
        return null;
    }
}
