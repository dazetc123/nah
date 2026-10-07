package com.example.betong.repository;

import com.example.betong.entity.SuCo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuCoRepository extends JpaRepository<SuCo, Long> {
    Page<SuCo> findAllByXeIsNotNullOrderByThoiDiemDesc(Pageable pageable);

    /** Mục 2.2.3 - danh sách sự cố gắn với chuyến mà chính tài xế đã báo cáo. */
    Page<SuCo> findAllByTaiXe_IdTXAndChuyenIsNotNullOrderByThoiDiemDesc(Long idTX, Pageable pageable);
}
