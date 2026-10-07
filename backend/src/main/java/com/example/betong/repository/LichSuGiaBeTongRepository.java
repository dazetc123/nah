package com.example.betong.repository;

import com.example.betong.entity.LichSuGiaBeTong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LichSuGiaBeTongRepository extends JpaRepository<LichSuGiaBeTong, Long> {
    List<LichSuGiaBeTong> findByLoaiBeTong_IdLBTOrderByNgayApDungDesc(Long idLBT);
}
