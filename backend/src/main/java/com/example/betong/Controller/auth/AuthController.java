package com.example.betong.Controller.auth;

import com.example.betong.DTO.request.auth.DoiMatKhauLanDauRequest;
import com.example.betong.DTO.request.auth.LoginRequest;
import com.example.betong.DTO.request.auth.RegisterRequest;
import com.example.betong.DTO.response.auth.LoginResponse;
import com.example.betong.DTO.response.auth.RegisterResponse;
import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.request.auth.QuenMatKhauRequest;
import com.example.betong.DTO.request.auth.DatLaiMatKhauRequest;
import com.example.betong.Service.auth.AuthService;
import com.example.betong.Security.JwtUtil;
import com.example.betong.Security.TokenRevocationService;
import com.example.betong.DTO.response.auth.DangXuatResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final TokenRevocationService tokenRevocationService;

    public AuthController(AuthService authService,
                          JwtUtil jwtUtil,
                          TokenRevocationService tokenRevocationService) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.tokenRevocationService = tokenRevocationService;
    }

    @PostMapping("/dang-nhap")
    public ResponseEntity<LoginResponse> dangNhap(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.dangNhap(request));
    }

    /**
     * Điểm bắt đầu đăng nhập Google được hiển thị trong Swagger.
     * Spring Security tiếp tục xử lý OAuth2 tại /oauth2/authorization/google.
     */
    @GetMapping("/dang-nhap-google")
    public ResponseEntity<Void> dangNhapBangGoogle() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/oauth2/authorization/google"))
                .build();
    }

    @PostMapping("/dang-ky")
    public ResponseEntity<RegisterResponse> dangKy(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.dangKy(request));
    }

    @PostMapping("/quen-mat-khau")
    public ResponseEntity<ThongBaoResponse> quenMatKhau(@Valid @RequestBody QuenMatKhauRequest request) {
        return ResponseEntity.ok(authService.guiOtpQuenMatKhau(request));
    }

    @PostMapping("/dat-lai-mat-khau")
    public ResponseEntity<ThongBaoResponse> datLaiMatKhau(@Valid @RequestBody DatLaiMatKhauRequest request) {
        return ResponseEntity.ok(authService.datLaiMatKhau(request));
    }

    @PostMapping("/dang-xuat")
    public ResponseEntity<DangXuatResponse> dangXuat(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = "BETONG_ACCESS_TOKEN", required = false) String cookieToken,
            HttpServletResponse response) {
        if ((authorization == null || !authorization.startsWith("Bearer ")) && cookieToken != null) {
            authorization = "Bearer " + cookieToken;
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(new DangXuatResponse("Thiếu token Bearer để đăng xuất"));
        }
        String token = authorization.substring(7).trim();
        if (token.isEmpty() || !jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new DangXuatResponse("Token không hợp lệ hoặc đã hết hạn"));
        }
        tokenRevocationService.revoke(token, jwtUtil.parseClaims(token));
        Cookie expiredCookie = new Cookie("BETONG_ACCESS_TOKEN", "");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(false);
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);
        return ResponseEntity.ok(new DangXuatResponse("Đăng xuất thành công"));
    }

    /** Yêu cầu JWT hợp lệ (Header Authorization: Bearer ...) dù đang bị buộc đổi mật khẩu. */
    @PostMapping("/doi-mat-khau-lan-dau")
    public ResponseEntity<LoginResponse> doiMatKhauLanDau(@Valid @RequestBody DoiMatKhauLanDauRequest request,
                                                          Authentication authentication) {
        String tenDangNhap = authentication.getName();
        return ResponseEntity.ok(authService.doiMatKhauLanDau(tenDangNhap, request));
    }
}