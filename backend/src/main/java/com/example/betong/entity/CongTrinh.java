package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bảng 3.83 - Bảng cơ sở dữ liệu công trình
 */
@Entity
@Table(name = "cong_trinh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CongTrinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCT")
    private Long idCT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKH", referencedColumnName = "idKH", nullable = false)
    private KhachHang khachHang;

    @Column(name = "tenCongTrinh", length = 150)
    private String tenCongTrinh;

    @Column(name = "diaChi", length = 255)
    private String diaChi;

    @Column(name = "viDo")
    private Double viDo;

    @Column(name = "kinhDo")
    private Double kinhDo;

    @Column(name = "SDT", length = 15)
    private String sdt;

    @OneToMany(mappedBy = "congTrinh")
    private List<DonHang> danhSachDonHang;
}