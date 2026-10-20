package com.example.betong.Controller.user;

import com.example.betong.DTO.request.user.PhanQuyenRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.user.TaiKhoanResponse;
import com.example.betong.Service.user.PhanQuyenService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Bảng 3.11 - Phân quyền người dùng. */
@RestController
@RequestMapping("/api/quan-ly/phan-quyen")
public class PhanQuyenController {

    private final PhanQuyenService service;

    public PhanQuyenController(PhanQuyenService service) {
        this.service = service;
    }

    /** Bước 2: danh sách TẤT CẢ tài khoản (không lọc vai trò). */
    @GetMapping
    public PageResponse<TaiKhoanResponse> danhSachTatCa(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") int trang,
            @RequestParam(defaultValue = "20") int soLuong) {
        return service.danhSachTatCaTaiKhoan(tuKhoa, trang, soLuong);
    }

    /** Bước 3-6: đổi vai trò cho 1 tài khoản. */
    @PutMapping("/{id}")
    public TaiKhoanResponse doiVaiTro(
            @PathVariable Long id,
            @Valid @RequestBody PhanQuyenRequest request,
            Authentication authentication) {
        return service.doiVaiTro(id, request, authentication.getName());
    }
}