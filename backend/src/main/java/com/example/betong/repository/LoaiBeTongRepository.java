package com.example.betong.repository;

import com.example.betong.entity.LoaiBeTong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoaiBeTongRepository extends JpaRepository<LoaiBeTong, Long> {

    boolean existsByMacBeTongIgnoreCase(String macBeTong);

    boolean existsByMacBeTongIgnoreCaseAndIdLBTNot(String macBeTong, Long idLBT);

    /** Bảng 3.52 - Tìm kiếm loại bê tông: 1 thanh tìm kiếm, tìm theo mác hoặc thành phần. */
    @Query("""
            SELECT l FROM LoaiBeTong l
            WHERE (:tuKhoa IS NULL
                   OR LOWER(l.macBeTong) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(l.thanhPhan) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
            ORDER BY l.idLBT DESC
            """)
    Page<LoaiBeTong> timKiem(@Param("tuKhoa") String tuKhoa, Pageable pageable);
}
