package com.example.betong.repository;

import com.example.betong.entity.Chuyen;
import com.example.betong.entity.Xe;
import com.example.betong.entity.TaiXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDateTime;

public interface ChuyenRepository extends JpaRepository<Chuyen, Long> {
    boolean existsByXe_IdXe(Long idXe);
    boolean existsByTramTron_IdTram(Long idTram);
    boolean existsByTaiXe_IdTX(Long idTX);

    boolean existsByDonHang_IdDH(Long idDH);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Chuyen c
            WHERE c.xe.idXe = :idXe AND c.trangThai IN (0, 1, 2, 3, 4)
            """)
    boolean xeDangBan(@Param("idXe") Long idXe);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Chuyen c
            WHERE c.taiXe.idTX = :idTX AND c.trangThai IN (0, 1, 2, 3, 4)
            """)
    boolean taiXeDangBan(@Param("idTX") Long idTX);

    @Query("""
            SELECT x FROM Xe x
            WHERE x.trangThai = 1
              AND (:tuKhoa IS NULL OR LOWER(x.bienSo) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(x.taiXe.hoTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
              AND NOT EXISTS (SELECT c.idChuyen FROM Chuyen c
                              WHERE c.xe = x AND c.trangThai IN (0, 1, 2, 3, 4))
            ORDER BY x.idXe DESC
            """)
    Page<Xe> timXeRanh(@Param("tuKhoa") String tuKhoa, Pageable pageable);

    @Query("""
            SELECT t FROM TaiXe t
            WHERE t.trangThai = 1
              AND (:tuKhoa IS NULL OR LOWER(t.hoTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(t.sdt) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(t.soGPLX) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
              AND NOT EXISTS (SELECT c.idChuyen FROM Chuyen c
                              WHERE c.taiXe = t AND c.trangThai IN (0, 1, 2, 3, 4))
            ORDER BY t.idTX DESC
            """)
    Page<TaiXe> timTaiXeRanh(@Param("tuKhoa") String tuKhoa, Pageable pageable);

    @Query("""
            SELECT c FROM Chuyen c
            WHERE c.trangThai IN (0, 1, 2, 3, 4)
            ORDER BY c.idChuyen DESC
            """)
    Page<Chuyen> timChuyenDangHoatDong(Pageable pageable);

    long countByThoiGianXuatPhatBetween(LocalDateTime tuNgay, LocalDateTime denNgay);

    // ===================== Dành cho app Tài xế (mục 2.2.1) =====================

    /**
     * Bảng usecase "Xem danh sách chuyến được phân công" — bước 5: lọc theo trạng thái
     * và/hoặc theo ngày giao dự kiến [tuNgay, denNgay) (đều tuỳ chọn).
     */
    @Query("""
            SELECT c FROM Chuyen c
            WHERE c.taiXe.idTX = :idTX
              AND (:trangThai IS NULL OR c.trangThai = :trangThai)
              AND (:tuNgay IS NULL OR c.donHang.thoiGianGiao >= :tuNgay)
              AND (:denNgay IS NULL OR c.donHang.thoiGianGiao < :denNgay)
            ORDER BY c.idChuyen DESC
            """)
    Page<Chuyen> timCuaTaiXe(@Param("idTX") Long idTX, @Param("trangThai") Integer trangThai,
                             @Param("tuNgay") LocalDateTime tuNgay, @Param("denNgay") LocalDateTime denNgay,
                             Pageable pageable);

    /** Lấy 1 chuyến NHƯNG chỉ khi đúng là của tài xế này — chặn xem/sửa chuyến người khác. */
    @Query("""
            SELECT c FROM Chuyen c
            WHERE c.idChuyen = :idChuyen AND c.taiXe.idTX = :idTX
            """)
    java.util.Optional<Chuyen> timCuaTaiXe(@Param("idChuyen") Long idChuyen, @Param("idTX") Long idTX);

    /** Luồng phụ 5.b "Nhận chuyến": tài xế đang có chuyến khác đã nhận nhưng chưa hoàn thành. */
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Chuyen c
            WHERE c.taiXe.idTX = :idTX AND c.idChuyen <> :idChuyen AND c.trangThai IN (1, 2, 3, 4)
            """)
    boolean taiXeCoChuyenKhacChuaXong(@Param("idTX") Long idTX, @Param("idChuyen") Long idChuyen);

    /** "Hoàn thành chuyến": đơn hàng còn chuyến nào khác chưa hoàn thành thì chưa đóng đơn. */
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Chuyen c
            WHERE c.donHang.idDH = :idDH AND c.idChuyen <> :idChuyen AND c.trangThai IN (0, 1, 2, 3, 4)
            """)
    boolean donHangConChuyenKhacChuaXong(@Param("idDH") Long idDH, @Param("idChuyen") Long idChuyen);

    /** "Xác nhận giao hàng thành công" bước 6: cộng dồn khối lượng đã giao của đơn hàng. */
    @Query("""
            SELECT COALESCE(SUM(c.khoiLuongThucGiao), 0) FROM Chuyen c
            WHERE c.donHang.idDH = :idDH AND c.trangThai IN (4, 5)
            """)
    Double tongKhoiLuongDaGiao(@Param("idDH") Long idDH);

    /** "Báo cáo sự cố": chuyến tài xế đang thực hiện (Đã nhận -> Đã giao hàng), mới nhất trước. */
    @Query("""
            SELECT c FROM Chuyen c
            WHERE c.taiXe.idTX = :idTX AND c.trangThai IN (1, 2, 3, 4)
            ORDER BY c.idChuyen DESC
            """)
    List<Chuyen> timChuyenDangThucHien(@Param("idTX") Long idTX);
}
