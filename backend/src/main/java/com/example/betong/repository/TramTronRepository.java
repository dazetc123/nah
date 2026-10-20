package com.example.betong.repository;

import com.example.betong.entity.TramTron;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TramTronRepository extends JpaRepository<TramTron, Long> {

    boolean existsByTenTramIgnoreCase(String tenTram);

    boolean existsByTenTramIgnoreCaseAndIdTramNot(String tenTram, Long idTram);

    /**
     * Bảng 3.21 - Tìm kiếm trạm trộn: 1 thanh tìm kiếm duy nhất, tìm theo
     * tên trạm hoặc địa chỉ. tuKhoa rỗng/null = xem tất cả (bước 2 khi
     * chưa nhập từ khóa).
     */
    @Query("""
            SELECT t FROM TramTron t
            WHERE (:tuKhoa IS NULL
                   OR LOWER(t.tenTram) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(t.diaChi) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
            ORDER BY t.idTram DESC
            """)
    Page<TramTron> timKiem(@Param("tuKhoa") String tuKhoa, Pageable pageable);

    @Query("SELECT t FROM TramTron t WHERE t.trangThai = 1 AND t.congSuat >= :khoiLuong ORDER BY t.idTram")
    List<TramTron> timKhaDung(@Param("khoiLuong") Double khoiLuong);
}
