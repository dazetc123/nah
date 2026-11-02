package com.example.betong.repository;

import com.example.betong.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {

    /**
     * Usecase Đăng nhập bước 3: tác nhân có thể chọn đăng nhập bằng
     * "email" hoặc "tài khoản đã đăng ký" (tenDangNhap) — nên tìm theo
     * cả hai trường bằng một tham số duy nhất.
     */
    @Query("SELECT tk FROM TaiKhoan tk WHERE tk.tenDangNhap = :dinhDanh OR tk.email = :dinhDanh")
    Optional<TaiKhoan> findByTenDangNhapOrEmail(@Param("dinhDanh") String dinhDanh);

    /** Usecase Đăng ký bước 5: kiểm tra trùng tên đăng nhập trước khi lưu (5.a/5.b mở rộng). */
    boolean existsByTenDangNhap(String tenDangNhap);

    /** Usecase Đăng ký bước 5: kiểm tra trùng email trước khi lưu. */
    boolean existsByEmail(String email);

    Optional<TaiKhoan> findBySdt(String sdt);

    @Query("""
            SELECT tk FROM TaiKhoan tk
            WHERE (:tuKhoa IS NULL OR
                   LOWER(tk.tenDangNhap) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR
                   LOWER(tk.hoTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR
                   LOWER(tk.email) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
              AND (:tenVaiTro IS NULL OR tk.vaiTro.tenVaiTro = :tenVaiTro)
            ORDER BY tk.idTK DESC
            """)
    Page<TaiKhoan> timKiem(@Param("tuKhoa") String tuKhoa,
                           @Param("tenVaiTro") String tenVaiTro,
                           Pageable pageable);
}