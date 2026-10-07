package com.example.betong.DTO.response.auth;

import lombok.Getter;

/** Kết quả trả về khi "Thông báo đăng nhập thành công" (bước 6 của usecase Đăng nhập). */
@Getter
public class LoginResponse {
    private final String accessToken;
    private final String tokenType = "Bearer";
    private final Long idTK;
    private final String hoTen;
    private final String tenVaiTro;
    /** true = tài khoản được Quản lý cấp, bắt buộc đổi mật khẩu trước khi dùng tiếp. */
    private final boolean phaiDoiMatKhau;

    public LoginResponse(String accessToken, Long idTK, String hoTen, String tenVaiTro, boolean phaiDoiMatKhau) {
        this.accessToken = accessToken;
        this.idTK = idTK;
        this.hoTen = hoTen;
        this.tenVaiTro = tenVaiTro;
        this.phaiDoiMatKhau = phaiDoiMatKhau;
    }
}