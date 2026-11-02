package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Bảng 3.86 - Bảng cơ sở dữ liệu lịch sử giá bê tông
 */
@Entity
@Table(name = "lich_su_gia_be_tong")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuGiaBeTong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLSG")
    private Long idLSG;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idLBT", referencedColumnName = "idLBT", nullable = false)
    private LoaiBeTong loaiBeTong;

    @Column(name = "donGia")
    private Double donGia;

    @Column(name = "ngayApDung")
    private LocalDate ngayApDung;
}
