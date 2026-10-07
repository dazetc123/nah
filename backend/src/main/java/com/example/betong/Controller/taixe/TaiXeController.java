package com.example.betong.Controller.taixe;

import com.example.betong.DTO.request.taixe.SuaTaiXeRequest;
import com.example.betong.DTO.request.taixe.ThemTaiXeRequest;
import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixe.TaiXeResponse;
import com.example.betong.Service.taixe.TaiXeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Bảng 3.23-3.26 - Quản lý tài xế.
 * Bảng 3.27 (Gán tài xế cho xe) nằm ở XeController: PUT/DELETE
 * /api/quan-ly/xe/{id}/tai-xe — không lặp lại route ở đây.
 */
@RestController
@RequestMapping("/api/quan-ly/tai-xe")
public class TaiXeController {

    private final TaiXeService service;

    public TaiXeController(TaiXeService service) {
        this.service = service;
    }

    /** Bảng 3.26 - Tìm kiếm (tuKhoa rỗng = xem tất cả). */
    @GetMapping
    public PageResponse<TaiXeResponse> danhSach(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSach(tuKhoa, trang, soLuong);
    }

    @GetMapping("/{id}")
    public TaiXeResponse xemChiTiet(@PathVariable Long id) {
        return service.layChiTiet(id);
    }

    /** Bảng 3.23 - Thêm tài xế. */
    @PostMapping
    public ResponseEntity<TaiXeResponse> them(@Valid @RequestBody ThemTaiXeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.them(request));
    }

    /**
     * API tương thích client cũ; luôn từ chối cập nhật để hồ sơ chỉ có một
     * nguồn ghi là /api/nguoi-dung/ho-so.
     */
    @PutMapping("/{id}")
    public TaiXeResponse sua(@PathVariable Long id, @Valid @RequestBody SuaTaiXeRequest request) {
        return service.sua(id, request);
    }

    /** Bảng 3.24 - Xóa tài xế (chỉ xóa hồ sơ, giữ nguyên tài khoản đăng nhập). */
    @DeleteMapping("/{id}")
    public ResponseEntity<ThongBaoResponse> xoa(@PathVariable Long id) {
        service.xoa(id);
        return ResponseEntity.ok(new ThongBaoResponse("Xóa tài xế thành công"));
    }
}
