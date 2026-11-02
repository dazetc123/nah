package com.example.betong.DTO.request.xe;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XeRequest {

    @NotBlank(message = "Biển số không được để trống")
    @Pattern(regexp = "^[0-9]{2}[A-Za-z]{1,2}[- ]?[0-9]{4,5}$",
            message = "Biển số xe không đúng định dạng")
    private String bienSo;

    @NotNull(message = "Trọng tải không được để trống")
    @DecimalMin(value = "0.01", message = "Trọng tải phải lớn hơn 0")
    private Double trongTai;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;

}
