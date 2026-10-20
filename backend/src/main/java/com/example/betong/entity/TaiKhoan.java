package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng "tai_khoan"
 * Bảng 3.89 - Bảng cơ sở dữ liệu tài khoản
 */
@Entity
@Table(name = "tai_khoan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTK")
    private Long idTK;

    @Column(name = "tenDangNhap", length = 50, unique = true, nullable = false)
    private String tenDangNhap;

    // Lưu mật khẩu đã hash bằng BCrypt, KHÔNG lưu plaintext
    @Column(name = "matKhau", length = 255, nullable = false)
    private String matKhau;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "SDT", length = 15)
    private String sdt;

    @Column(name = "hoTen", length = 100)
    private String hoTen;

    @Column(name = "anhDaiDien", length = 500)
    private String anhDaiDien;

    @Column(name = "ngaySinh")
    private java.time.LocalDate ngaySinh;

    @Column(name = "diaChiThuongTru", length = 255)
    private String diaChiThuongTru;

    @Column(name = "gioiTinh", length = 20)
    private String gioiTinh;

    // 1: đang hoạt động, 0: bị khóa
    @Column(name = "trangThai")
    private Integer trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVaiTro", referencedColumnName = "idVaiTro", nullable = false)
    private VaiTro vaiTro;

    // Mã OTP dùng cho xác thực đăng ký / quên mật khẩu
    @Column(name = "maXacThuc", length = 10)
    private String maXacThuc;

    @Column(name = "maXacThucHash", length = 100)
    private String maXacThucHash;

    @Column(name = "otpMucDich", length = 30)
    private String otpMucDich;

    // Thời điểm mã OTP hết hạn
    @Column(name = "maXacThucHetHan")
    private LocalDateTime maXacThucHetHan;

    // 0: chưa xác thực, 1: đã xác thực
    @Column(name = "daXacThuc")
    private Integer daXacThuc;

    @Column(name = "phaidoimatkhau", nullable = false)
    private Integer phaiDoiMatKhau = 0;
}
