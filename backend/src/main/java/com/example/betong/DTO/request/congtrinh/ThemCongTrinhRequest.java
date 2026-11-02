package com.example.betong.DTO.request.congtrinh;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThemCongTrinhRequest {

    @NotNull(message = "Vui lòng chọn khách hàng")
    private Long idKH;

    @NotBlank(message = "Vui lòng nhập tên công trình")
    private String tenCongTrinh;

    @NotBlank(message = "Vui lòng nhập địa chỉ công trình")
    private String diaChi;

    @NotNull(message = "Vui lòng nhập vĩ độ")
    @DecimalMin(value = "-90", message = "Vĩ độ phải nằm trong khoảng -90 đến 90")
    @DecimalMax(value = "90", message = "Vĩ độ phải nằm trong khoảng -90 đến 90")
    private Double viDo;

    @NotNull(message = "Vui lòng nhập kinh độ")
    @DecimalMin(value = "-180", message = "Kinh độ phải nằm trong khoảng -180 đến 180")
    @DecimalMax(value = "180", message = "Kinh độ phải nằm trong khoảng -180 đến 180")
    private Double kinhDo;

    @NotBlank(message = "Vui lòng nhập số điện thoại công trình")
    @Pattern(regexp = "^0[0-9]{9}$",
            message = "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)")
    private String sdt;
}
