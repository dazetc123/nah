package com.example.betong.Controller.suco;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.suco.SuCoResponse;
import com.example.betong.Service.suco.SuCoTaiXeService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mục 2.2.3 "Báo cáo sự cố" bước 6 và luồng 6.a - phía Nhân viên điều phối:
 * xem các sự cố tài xế gửi lên (ưu tiên cao, chưa xử lý lên đầu) và cập nhật
 * trạng thái xử lý. SecurityConfig giới hạn /api/dieu-phoi/** cho Điều phối, Quản lý.
 */
@RestController
@RequestMapping("/api/dieu-phoi/su-co")
public class SuCoDieuPhoiController {

    private final SuCoTaiXeService service;

    public SuCoDieuPhoiController(SuCoTaiXeService service) {
        this.service = service;
    }

    /** trangThai: 0 = Mới tiếp nhận, 1 = Đang xử lý, 2 = Đã xử lý; bỏ trống = tất cả. */
    @GetMapping
    public PageResponse<SuCoResponse> danhSach(@RequestParam(required = false) Integer trangThai,
                                               @RequestParam(defaultValue = "1") @Min(1) int trang,
                                               @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSachChoDieuPhoi(trangThai, trang, soLuong);
    }

    /** Body: {"trangThai": 1} hoặc {"trangThai": 2}. */
    @PutMapping("/{id}/trang-thai")
    public SuCoResponse capNhatTrangThai(@PathVariable("id") Long idSuCo, @RequestBody Map<String, Integer> body) {
        return service.capNhatTrangThai(idSuCo, body == null ? null : body.get("trangThai"));
    }
}
