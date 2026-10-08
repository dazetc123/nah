package com.example.betong.repository;


import com.example.betong.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findByTaiKhoanIdTK(Long idTK);
}
