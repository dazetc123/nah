package com.example.betong.repository;

import com.example.betong.entity.CongTrinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CongTrinhRepository extends JpaRepository<CongTrinh, Long> {

    @Query("""
            SELECT c FROM CongTrinh c
            WHERE (:tuKhoa IS NULL
                   OR LOWER(c.tenCongTrinh) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(c.diaChi) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(c.sdt) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
            ORDER BY c.idCT DESC
            """)
    Page<CongTrinh> timKiem(@Param("tuKhoa") String tuKhoa, Pageable pageable);

    @Query("""
            SELECT c FROM CongTrinh c
            WHERE c.khachHang.idKH = :idKH
              AND (:tuKhoa IS NULL
                   OR LOWER(c.tenCongTrinh) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(c.diaChi) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(c.sdt) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
            ORDER BY c.idCT DESC
            """)
    Page<CongTrinh> timKiemCuaKhachHang(@Param("idKH") Long idKH,
                                        @Param("tuKhoa") String tuKhoa,
                                        Pageable pageable);
}
