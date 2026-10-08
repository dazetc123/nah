package com.example.betong.repository;

import com.example.betong.entity.Xe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface XeRepository extends JpaRepository<Xe, Long> {

    boolean existsByBienSoIgnoreCase(String bienSo);

    boolean existsByBienSoIgnoreCaseAndIdXeNot(String bienSo, Long idXe);

    java.util.List<Xe> findByTaiXe_IdTX(Long idTX);

    @Query("""
            SELECT x FROM Xe x
            WHERE (:bienSo IS NULL OR LOWER(x.bienSo) LIKE LOWER(CONCAT('%', :bienSo, '%')))
              AND (:trangThai IS NULL OR x.trangThai = :trangThai)
            ORDER BY x.idXe DESC
            """)
    Page<Xe> timKiem(@Param("bienSo") String bienSo,
                     @Param("trangThai") Integer trangThai,
                     Pageable pageable);
}
