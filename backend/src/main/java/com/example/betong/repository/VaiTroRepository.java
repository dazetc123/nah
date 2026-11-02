package com.example.betong.repository;

import com.example.betong.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    Optional<VaiTro> findByTenVaiTro(String tenVaiTro);
}
