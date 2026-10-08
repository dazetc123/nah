package com.example.betong.repository;

import com.example.betong.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {
    boolean existsByCongTrinh_IdCT(Long idCT);

    List<DonHang> findByNgayDatBetweenOrderByNgayDatAscIdDHAsc(LocalDate tuNgay, LocalDate denNgay);

    Page<DonHang> findByTrangThaiOrderByNgayDatAscIdDHAsc(Integer trangThai, Pageable pageable);

    @Query("""
            SELECT d FROM DonHang d
            JOIN d.khachHang kh
            JOIN kh.taiKhoan tk
            WHERE (tk.tenDangNhap = :dinhDanh OR tk.email = :dinhDanh)
              AND (:tuKhoa IS NULL OR str(d.idDH) LIKE CONCAT('%', :tuKhoa, '%')
                   OR LOWER(d.congTrinh.diaChi) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(d.ghiChu) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
            ORDER BY d.idDH DESC
            """)
    Page<DonHang> timCuaKhach(@Param("dinhDanh") String dinhDanh,
                              @Param("tuKhoa") String tuKhoa, Pageable pageable);

    @Query("""
            SELECT d FROM DonHang d
            JOIN d.khachHang kh
            JOIN kh.taiKhoan tk
            WHERE d.idDH = :id AND (tk.tenDangNhap = :dinhDanh OR tk.email = :dinhDanh)
            """)
    java.util.Optional<DonHang> findCuaKhach(@Param("dinhDanh") String dinhDanh, @Param("id") Long id);

    /** Trang web điều phối, tab "Đã phân bổ trạm": chỉ đơn CHƯA có chuyến nào (mới tạo chuyến được). */
    @org.springframework.data.jpa.repository.Query("""
            SELECT d FROM DonHang d
            WHERE d.trangThai = :trangThai
              AND NOT EXISTS (SELECT c.idChuyen FROM Chuyen c WHERE c.donHang = d)
            ORDER BY d.ngayDat ASC, d.idDH ASC
            """)
    Page<DonHang> timChuaCoChuyen(@org.springframework.data.repository.query.Param("trangThai") Integer trangThai,
                                  Pageable pageable);
}
