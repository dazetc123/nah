package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bảng 3.85 - Bảng cơ sở dữ liệu khách hàng
 */
@Entity
@Table(name = "khach_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idKH")
    private Long idKH;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTK", referencedColumnName = "idTK", nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "tenKhachHang", length = 100)
    private String tenKhachHang;

    @Column(name = "diaChi", length = 255)
    private String diaChi;

    @Column(name = "SDT", length = 15)
    private String sdt;

    @Column(name = "email", length = 100)
    private String email;

    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL)
    private List<CongTrinh> danhSachCongTrinh;

    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL)
    private List<DonHang> danhSachDonHang;
}
