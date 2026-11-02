package com.example.betong.Controller.betong;

import com.example.betong.DTO.request.betong.LoaiBeTongRequest;
import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.response.betong.LoaiBeTongResponse;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.Service.betong.LoaiBeTongService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Bảng 3.50-3.54 - Quản lý bê tông (loại bê tông, đơn giá). */
@RestController
@RequestMapping("/api/quan-ly/loai-be-tong")
public class LoaiBeTongController {

    private final LoaiBeTongService service;

    public LoaiBeTongController(LoaiBeTongService service) {
        this.service = service;
    }

    /** Bảng 3.52 - Tìm kiếm (tuKhoa rỗng = xem tất cả). */
    @GetMapping
    public PageResponse<LoaiBeTongResponse> danhSach(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSach(tuKhoa, trang, soLuong);
    }

    /** Bảng 3.54 - Xem chi tiết (kèm số đơn hàng đã sử dụng). */
    @GetMapping("/{id}")
    public LoaiBeTongResponse xemChiTiet(@PathVariable Long id) {
        return service.xemChiTiet(id);
    }

    /** Bảng 3.50 - Thêm mới loại bê tông. */
    @PostMapping
    public ResponseEntity<LoaiBeTongResponse> them(@Valid @RequestBody LoaiBeTongRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.them(request));
    }

    /** Bảng 3.51 - Chỉnh sửa loại bê tông. */
    @PutMapping("/{id}")
    public LoaiBeTongResponse sua(@PathVariable Long id, @Valid @RequestBody LoaiBeTongRequest request) {
        return service.sua(id, request);
    }

    /** Bảng 3.53 - Xóa loại bê tông. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ThongBaoResponse> xoa(@PathVariable Long id) {
        service.xoa(id);
        return ResponseEntity.ok(new ThongBaoResponse("Xóa loại bê tông thành công"));
    }
}
