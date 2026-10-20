package com.example.betong.DTO.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TrangThaiRequest {
    @NotNull(message = "Vui lòng nhập trạng thái")
    private Integer trangThai;
}
