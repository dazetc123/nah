package com.example.betong.DTO.response.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TaiKhoanResponse {
    private Long idTK;
    private String tenDangNhap;
    private String email;
    private String sdt;
    private String hoTen;
    private String anhDaiDien;
    private LocalDate ngaySinh;
    private String diaChiThuongTru;
    private String gioiTinh;
    private Integer trangThai;
    private String tenVaiTro;
    private Integer phaiDoiMatKhau;
    private String soGPLX;
    private String diaChi;
    private Long idKH;
    private String tenKhachHang;
    private String thongBao;
}
