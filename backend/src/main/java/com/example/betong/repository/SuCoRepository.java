package com.example.betong.repository;

import com.example.betong.entity.SuCo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SuCoRepository extends JpaRepository<SuCo, Long> {
    Page<SuCo> findAllByXeIsNotNullOrderByThoiDiemDesc(Pageable pageable);

    /** Trang "Báo cáo lỗi xe" của Quản lý: chỉ báo cáo tình trạng xe, không lẫn sự cố trong chuyến. */
    Page<SuCo> findAllByXeIsNotNullAndChuyenIsNullOrderByThoiDiemDesc(Pageable pageable);

    /** Mục 2.2.3 - danh sách sự cố gắn với chuyến mà chính tài xế đã báo cáo. */
    Page<SuCo> findAllByTaiXe_IdTXAndChuyenIsNotNullOrderByThoiDiemDesc(Long idTX, Pageable pageable);

    /** Điều phối xem sự cố gắn với chuyến, lọc theo trạng thái xử lý (tuỳ chọn); ưu tiên cao lên trước. */
    @Query("""
            SELECT s FROM SuCo s
            WHERE s.chuyen IS NOT NULL AND (:trangThai IS NULL OR s.trangThai = :trangThai)
            ORDER BY s.trangThai ASC, s.mucDoUuTien DESC, s.thoiDiem DESC
            """)
    Page<SuCo> timChoDieuPhoi(@Param("trangThai") Integer trangThai, Pageable pageable);
}
