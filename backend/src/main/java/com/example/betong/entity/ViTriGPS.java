package com.example.betong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bảng 3.93 - Bảng cơ sở dữ liệu vị trí GPS
 */
@Entity
@Table(name = "vi_tri_gps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViTriGPS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idViTri")
    private Long idViTri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idChuyen", referencedColumnName = "idChuyen", nullable = false)
    private Chuyen chuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idXe", referencedColumnName = "idXe", nullable = false)
    private Xe xe;

    @Column(name = "viDo")
    private Double viDo;

    @Column(name = "kinhDo")
    private Double kinhDo;

    @Column(name = "tocDo")
    private Double tocDo;

    @Column(name = "thoiDiem")
    private LocalDateTime thoiDiem;
}
