package com.example.betong.DTO.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuenMatKhauRequest {
    @NotBlank(message = "Vui lòng nhập email, số điện thoại hoặc tên đăng nhập")
    private String dinhDanh;

    private KenhGui kenh = KenhGui.EMAIL;

    public enum KenhGui {
        EMAIL, SMS
    }
}
