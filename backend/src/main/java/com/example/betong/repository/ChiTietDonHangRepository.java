package com.example.betong.repository;

import com.example.betong.entity.ChiTietDonHang;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Long> {

    /** Bảng 3.53 - "Loại bê tông đang được dùng" -> chặn xóa. */
    boolean existsByLoaiBeTong_IdLBT(Long idLBT);

    /** Bảng 3.54 - "kèm số lượng đơn hàng đã sử dụng loại bê tông này". */
    long countByLoaiBeTong_IdLBT(Long idLBT);
}
