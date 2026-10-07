package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bảng 3.82 - Bảng cơ sở dữ liệu chuyến
 */
@Entity
@Table(name = "chuyen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idChuyen")
    private Long idChuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDH", referencedColumnName = "idDH", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idXe", referencedColumnName = "idXe", nullable = false)
    private Xe xe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTX", referencedColumnName = "idTX", nullable = false)
    private TaiXe taiXe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTram", referencedColumnName = "idTram", nullable = false)
    private TramTron tramTron;

    @Column(name = "khoiLuong")
    private Double khoiLuong;

    @Column(name = "thoiGianXuatPhat")
    private LocalDateTime thoiGianXuatPhat;

    @Column(name = "thoiGianDen")
    private LocalDateTime thoiGianDen;

    // Trạng thái chuyến: đang giao / đã đến / hoàn thành...
    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "chuyen", cascade = CascadeType.ALL)
    private List<ViTriGPS> danhSachViTriGPS;

    @OneToMany(mappedBy = "chuyen", cascade = CascadeType.ALL)
    private List<SuCo> danhSachSuCo;
}
