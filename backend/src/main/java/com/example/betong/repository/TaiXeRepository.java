package com.example.betong.repository;

import com.example.betong.entity.TaiXe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaiXeRepository extends JpaRepository<TaiXe, Long> {

    Optional<TaiXe> findByTaiKhoanIdTK(Long idTK);

    boolean existsBySoGPLXIgnoreCase(String soGPLX);

    boolean existsBySoGPLXIgnoreCaseAndIdTXNot(String soGPLX, Long idTX);

    /** Bảng 3.26 - Tìm kiếm tài xế: 1 thanh tìm kiếm, tìm theo tên hoặc SĐT hoặc GPLX. */
    @Query("""
            SELECT t FROM TaiXe t
            WHERE (:tuKhoa IS NULL
                   OR LOWER(t.hoTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(t.sdt) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(t.soGPLX) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
            ORDER BY t.idTX DESC
            """)
    Page<TaiXe> timKiem(@Param("tuKhoa") String tuKhoa, Pageable pageable);
}
