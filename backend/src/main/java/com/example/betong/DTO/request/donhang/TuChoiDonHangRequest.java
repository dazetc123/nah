package com.example.betong.DTO.request.donhang;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TuChoiDonHangRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String lyDo;
}
