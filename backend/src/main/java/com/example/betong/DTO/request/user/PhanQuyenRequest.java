package com.example.betong.DTO.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Bảng 3.11 - Phân quyền người dùng: đổi vai trò cho 1 tài khoản. */
@Getter
@Setter
public class PhanQuyenRequest {

    @NotBlank(message = "Vui lòng chọn vai trò")
    private String tenVaiTroMoi;
}
