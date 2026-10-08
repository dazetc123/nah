package com.example.betong.DTO.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Kết quả trả về khi "Thông báo đăng ký thành công" (bước 7 của usecase Đăng ký). */
@Getter
@AllArgsConstructor
public class RegisterResponse {
    private Long idTK;
    private String tenDangNhap;
    private String message;
}
