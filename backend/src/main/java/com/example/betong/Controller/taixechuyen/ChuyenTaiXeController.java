package com.example.betong.Controller.taixechuyen;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenChiTietResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenDanhSachResponse;
import com.example.betong.Service.taixechuyen.ChuyenTaiXeService;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Mục 2.2.1 báo cáo - Quản lý chuyến (tác nhân Tài xế): Xem danh sách
 * chuyến được phân công, Xem chi tiết chuyến, Nhận chuyến, Bắt đầu
 * chuyến, Hoàn thành chuyến. Chỉ thao tác trên chuyến của CHÍNH tài xế
 * đang đăng nhập (lấy từ Authentication, không nhận idTX từ client).
 */
@RestController
@RequestMapping("/api/tai-xe/chuyen")
public class ChuyenTaiXeController {

    private final ChuyenTaiXeService service;

    public ChuyenTaiXeController(ChuyenTaiXeService service) {
        this.service = service;
    }

    /** Usecase "Xem danh sách chuyến được phân công" - trangThai bỏ trống = xem tất cả. */
    @GetMapping
    public PageResponse<ChuyenDanhSachResponse> danhSach(
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong,
            Authentication authentication) {
        return service.danhSachChuyen(authentication.getName(), trangThai, trang, soLuong);
    }

    /** Usecase "Xem thông tin chi tiết chuyến". */
    @GetMapping("/{id}")
    public ChuyenChiTietResponse chiTiet(@PathVariable("id") Long idChuyen, Authentication authentication) {
        return service.chiTietChuyen(authentication.getName(), idChuyen);
    }

    /** Usecase "Nhận chuyến": Chờ nhận -> Đã nhận. */
    @PostMapping("/{id}/nhan")
    public ChuyenChiTietResponse nhan(@PathVariable("id") Long idChuyen, Authentication authentication) {
        return service.nhanChuyen(authentication.getName(), idChuyen);
    }

    /** Usecase "Bắt đầu chuyến": Đã nhận -> Đang giao, ghi nhận thời điểm xuất phát. */
    @PostMapping("/{id}/bat-dau")
    public ChuyenChiTietResponse batDau(@PathVariable("id") Long idChuyen, Authentication authentication) {
        return service.batDauChuyen(authentication.getName(), idChuyen);
    }

    /** Usecase "Hoàn thành chuyến": -> Hoàn thành, trả xe/tài xế về Sẵn sàng. */
    @PostMapping("/{id}/hoan-thanh")
    public ChuyenChiTietResponse hoanThanh(@PathVariable("id") Long idChuyen, Authentication authentication) {
        return service.hoanThanhChuyen(authentication.getName(), idChuyen);
    }
}
