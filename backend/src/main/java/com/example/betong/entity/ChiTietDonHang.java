package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bảng 3.81 - Bảng cơ sở dữ liệu chi tiết đơn hàng
 */
@Entity
@Table(name = "chi_tiet_don_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietDonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCTDH")
    private Long idCTDH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDH", referencedColumnName = "idDH", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idLBT", referencedColumnName = "idLBT", nullable = false)
    private LoaiBeTong loaiBeTong;

    @Column(name = "khoiLuong")
    private Double khoiLuong;

    @Column(name = "donGia")
    private Double donGia;

    @Column(name = "thanhTien")
    private Double thanhTien;
}
