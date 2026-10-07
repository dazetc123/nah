package com.example.betong.Controller.taixechuyen;

import com.example.betong.DTO.request.taixechuyen.DaDenCongTrinhRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenChiTietResponse;
import com.example.betong.DTO.response.taixechuyen.ChuyenDanhSachResponse;
import com.example.betong.Service.taixechuyen.ChuyenTaiXeService;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mục 2.2.1 báo cáo - Quản lý chuyến (tác nhân Tài xế): Xem danh sách
 * chuyến được phân công, Xem chi tiết chuyến, Nhận chuyến, Bắt đầu
 * chuyến, Hoàn thành chuyến; mục 2.2.3 - Đã đến công trình, Xác nhận giao hàng. Chỉ thao tác trên chuyến của CHÍNH tài xế
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

    /** Mục 2.2.3 - Usecase "Xác nhận đã đến công trình": Đang giao -> Đã đến công trình. */
    @PostMapping("/{id}/da-den")
    public ChuyenChiTietResponse daDen(@PathVariable("id") Long idChuyen,
                                       @RequestBody(required = false) DaDenCongTrinhRequest request,
                                       Authentication authentication) {
        return service.xacNhanDaDen(authentication.getName(), idChuyen, request);
    }

    /** Mục 2.2.3 - Usecase "Xác nhận giao hàng thành công": Đã đến -> Đã giao hàng (multipart, ảnh tùy chọn). */
    @PostMapping(value = "/{id}/giao-hang", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChuyenChiTietResponse giaoHang(@PathVariable("id") Long idChuyen,
                                          @RequestParam(value = "khoiLuongThucGiao", required = false) Double khoiLuongThucGiao,
                                          @RequestParam(value = "ghiChu", required = false) String ghiChu,
                                          @RequestPart(value = "anhMinhChung", required = false) MultipartFile anhMinhChung,
                                          Authentication authentication) {
        return service.xacNhanGiaoHang(authentication.getName(), idChuyen, khoiLuongThucGiao, ghiChu, anhMinhChung);
    }

    /** Usecase "Hoàn thành chuyến": Đã giao hàng -> Hoàn thành, trả xe/tài xế về Sẵn sàng. */
    @PostMapping("/{id}/hoan-thanh")
    public ChuyenChiTietResponse hoanThanh(@PathVariable("id") Long idChuyen, Authentication authentication) {
        return service.hoanThanhChuyen(authentication.getName(), idChuyen);
    }
}
