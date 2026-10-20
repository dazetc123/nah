package com.example.betong.DTO.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Cấu trúc thông báo lỗi trả về cho các nhánh rẽ (4.a, 5.a, 5.b, sai mật khẩu, khóa tài khoản...). */
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String message;
}
