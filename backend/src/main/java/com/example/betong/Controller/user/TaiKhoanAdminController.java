package com.example.betong.Controller.user;

import com.example.betong.DTO.request.user.CapNhatTaiKhoanRequest;
import com.example.betong.DTO.request.user.TaoTaiKhoanRequest;
import com.example.betong.DTO.request.user.TrangThaiRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.user.TaiKhoanResponse;
import com.example.betong.DTO.response.user.TaoTaiKhoanResponse;
import com.example.betong.Service.user.TaiKhoanAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quan-ly/tai-khoan")
public class TaiKhoanAdminController {

    private final TaiKhoanAdminService service;

    public TaiKhoanAdminController(TaiKhoanAdminService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<TaiKhoanResponse> danhSachNhanVien(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") int trang,
            @RequestParam(defaultValue = "20") int soLuong) {
        return service.danhSachNhanVienDieuPhoiVaTaiXe(tuKhoa, trang, soLuong);
    }

    @GetMapping("/khach-hang")
    public PageResponse<TaiKhoanResponse> danhSachKhachHang(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") int trang,
            @RequestParam(defaultValue = "20") int soLuong) {
        return service.danhSachKhachHang(tuKhoa, trang, soLuong);
    }

    @GetMapping("/{id}")
    public TaiKhoanResponse xemChiTiet(@PathVariable Long id) {
        return service.xemChiTiet(id);
    }

    @GetMapping("/khach-hang/{id}")
    public TaiKhoanResponse xemChiTietKhachHang(@PathVariable Long id) {
        return service.xemChiTiet(id);
    }

    @GetMapping("/nhan-vien-dieu-phoi/{id}")
    public TaiKhoanResponse xemChiTietNhanVien(@PathVariable Long id) {
        return service.xemChiTiet(id);
    }

    @GetMapping("/tai-xe/{id}")
    public TaiKhoanResponse xemChiTietTaiXe(@PathVariable Long id) {
        return service.xemChiTiet(id);
    }

    @PostMapping
    public ResponseEntity<TaoTaiKhoanResponse> themTaiKhoan(
            @Valid @RequestBody TaoTaiKhoanRequest request,
            Authentication authentication) {
        TaoTaiKhoanResponse response = service.themTaiKhoan(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public TaiKhoanResponse suaTaiKhoan(
            @PathVariable Long id,
            @Valid @RequestBody CapNhatTaiKhoanRequest request,
            Authentication authentication) {
        return service.suaTaiKhoan(id, request, authentication.getName());
    }

    @PutMapping("/{id}/trang-thai")
    public TaiKhoanResponse khoaMoTaiKhoan(
            @PathVariable Long id,
            @Valid @RequestBody TrangThaiRequest request,
            Authentication authentication) {
        return service.khoaMoTaiKhoan(id, request, authentication.getName());
    }
}