package com.example.betong.repository;

import com.example.betong.entity.ViTriGPS;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViTriGPSRepository extends JpaRepository<ViTriGPS, Long> {
    boolean existsByXe_IdXe(Long idXe);

    java.util.Optional<ViTriGPS> findTopByChuyen_IdChuyenOrderByThoiDiemDesc(Long idChuyen);
}
