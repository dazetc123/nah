package com.example.betong.Controller.congtrinh;

import com.example.betong.DTO.request.congtrinh.CapNhatCongTrinhRequest;
import com.example.betong.DTO.request.congtrinh.ThemCongTrinhRequest;
import com.example.betong.DTO.response.congtrinh.CongTrinhResponse;
import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.Service.congtrinh.CongTrinhService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

/**
 * Quản lý công trình - tác nhân Quản lý.
 */
@RestController
@RequestMapping("/api/quan-ly/cong-trinh")
public class CongTrinhController {

    private final CongTrinhService service;

    public CongTrinhController(CongTrinhService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CongTrinhResponse> timKiem(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.timKiem(tuKhoa, trang, soLuong);
    }

    @PostMapping
    public ResponseEntity<CongTrinhResponse> them(@Valid @RequestBody ThemCongTrinhRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.them(request));
    }

    @GetMapping("/{id}")
    public CongTrinhResponse xemChiTiet(@PathVariable Long id) {
        return service.layChiTiet(id);
    }

    @PutMapping("/{id}")
    public CongTrinhResponse capNhat(@PathVariable Long id,
                                     @Valid @RequestBody CapNhatCongTrinhRequest request) {
        return service.capNhat(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ThongBaoResponse> xoa(@PathVariable Long id) {
        service.xoa(id);
        return ResponseEntity.ok(new ThongBaoResponse("Xóa công trình thành công"));
    }
}
