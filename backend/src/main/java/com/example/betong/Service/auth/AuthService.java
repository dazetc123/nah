package com.example.betong.Service.auth;

import com.example.betong.DTO.request.auth.DoiMatKhauLanDauRequest;
import com.example.betong.DTO.request.auth.LoginRequest;
import com.example.betong.DTO.request.auth.RegisterRequest;
import com.example.betong.DTO.response.auth.LoginResponse;
import com.example.betong.DTO.response.auth.RegisterResponse;
import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.request.auth.QuenMatKhauRequest;
import com.example.betong.DTO.request.auth.DatLaiMatKhauRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {
    LoginResponse dangNhap(LoginRequest request);

    RegisterResponse dangKy(RegisterRequest request);

    LoginResponse doiMatKhauLanDau(String tenDangNhap, DoiMatKhauLanDauRequest request);

    ThongBaoResponse guiOtpQuenMatKhau(QuenMatKhauRequest request);

    ThongBaoResponse datLaiMatKhau(DatLaiMatKhauRequest request);

    LoginResponse dangNhapBangGoogle(OAuth2User googleUser);
}
