package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entity ánh xạ bảng "vai_tro"
 * Bảng 3.92 - Bảng cơ sở dữ liệu vai trò
 */
@Entity
@Table(name = "vai_tro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVaiTro")
    private Long idVaiTro;

    @Column(name = "tenVaiTro", length = 50, unique = true, nullable = false)
    private String tenVaiTro; // Quản lý / Nhân viên điều phối / Tài xế / Khách hàng

    // Một vai trò có nhiều tài khoản
    @OneToMany(mappedBy = "vaiTro", cascade = CascadeType.ALL)
    private List<TaiKhoan> danhSachTaiKhoan;
}
