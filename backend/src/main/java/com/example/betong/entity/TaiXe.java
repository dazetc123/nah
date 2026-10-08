package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bảng 3.90 - Bảng cơ sở dữ liệu tài xế
 */
@Entity
@Table(name = "tai_xe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTX")
    private Long idTX;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTK", referencedColumnName = "idTK", nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "hoTen", length = 100)
    private String hoTen;

    @Column(name = "soGPLX", length = 30)
    private String soGPLX;

    @Column(name = "SDT", length = 15)
    private String sdt;

    // Trạng thái hoạt động của tài xế
    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "taiXe")
    private List<Xe> danhSachXe;

    @OneToMany(mappedBy = "taiXe")
    private List<Chuyen> danhSachChuyen;
}
