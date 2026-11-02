package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bảng 3.87 - Bảng cơ sở dữ liệu loại bê tông
 */
@Entity
@Table(name = "loai_be_tong")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoaiBeTong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLBT")
    private Long idLBT;

    @Column(name = "macBeTong", length = 50)
    private String macBeTong;

    @Column(name = "thanhPhan", length = 255)
    private String thanhPhan;

    // Đơn giá hiện tại (đơn giá theo mốc thời gian chi tiết xem LichSuGiaBeTong)
    @Column(name = "donGia")
    private Double donGia;

    @Column(name = "trangThai")
    private Integer trangThai;

    @Column(name = "moTa", length = 255)
    private String moTa;

    @OneToMany(mappedBy = "loaiBeTong", cascade = CascadeType.ALL)
    private List<LichSuGiaBeTong> lichSuGia;

    @OneToMany(mappedBy = "loaiBeTong")
    private List<ChiTietDonHang> chiTietDonHangs;
}
