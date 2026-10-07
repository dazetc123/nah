package com.example.betong.DTO.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class CapNhatHoSoRequest {
    @NotBlank(message = "Vui lòng nhập họ tên")
    private String hoTen;
    @Pattern(regexp = "^0[0-9]{9}$",
            message = "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)")
    private String sdt;
    @Email(message = "Email không đúng định dạng")
    private String email;
    private String diaChi;
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String diaChiThuongTru;
}
