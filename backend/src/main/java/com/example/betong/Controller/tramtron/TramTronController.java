package com.example.betong.Controller.tramtron;

import com.example.betong.DTO.request.tramtron.CongSuatRequest;
import com.example.betong.DTO.request.tramtron.TramTronRequest;
import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.tramtron.TramTronResponse;
import com.example.betong.Service.tramtron.TramTronService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Bảng 3.18-3.22 - Quản lý trạm trộn. */
@RestController
@RequestMapping("/api/quan-ly/tram-tron")
public class TramTronController {

    private final TramTronService service;

    public TramTronController(TramTronService service) {
        this.service = service;
    }

    /** Bảng 3.21 - Tìm kiếm (tuKhoa rỗng = xem tất cả, không có bảng "xem danh sách" riêng). */
    @GetMapping
    public PageResponse<TramTronResponse> danhSach(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSach(tuKhoa, trang, soLuong);
    }

    @GetMapping("/{id}")
    public TramTronResponse xemChiTiet(@PathVariable Long id) {
        return service.layChiTiet(id);
    }

    /** Bảng 3.18 - Thêm trạm trộn. */
    @PostMapping
    public ResponseEntity<TramTronResponse> them(@Valid @RequestBody TramTronRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.them(request));
    }

    /** Bảng 3.20 - Sửa trạm trộn. */
    @PutMapping("/{id}")
    public TramTronResponse sua(@PathVariable Long id, @Valid @RequestBody TramTronRequest request) {
        return service.sua(id, request);
    }

    /** Bảng 3.19 - Xóa trạm trộn. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ThongBaoResponse> xoa(@PathVariable Long id) {
        service.xoa(id);
        return ResponseEntity.ok(new ThongBaoResponse("Xóa trạm trộn thành công"));
    }

    /** Bảng 3.22 - Quản lý công suất (usecase riêng biệt, chỉ đổi công suất). */
    @PutMapping("/{id}/cong-suat")
    public TramTronResponse capNhatCongSuat(@PathVariable Long id, @Valid @RequestBody CongSuatRequest request) {
        return service.capNhatCongSuat(id, request);
    }
}
