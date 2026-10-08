package com.example.betong.Controller.congtrinh;

import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.congtrinh.CongTrinhResponse;
import jakarta.validation.constraints.Min;
import com.example.betong.Service.congtrinh.CongTrinhService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/khach-hang/cong-trinh")
public class CongTrinhKhachHangController {

    private final CongTrinhService service;

    public CongTrinhKhachHangController(CongTrinhService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CongTrinhResponse> timKiem(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong,
            Authentication authentication) {
        return service.timKiemCuaKhachHang(tuKhoa, authentication.getName(), trang, soLuong);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ThongBaoResponse> xoa(@PathVariable Long id,
                                                 Authentication authentication) {
        service.xoaCuaKhachHang(id, authentication.getName());
        return ResponseEntity.ok(new ThongBaoResponse("Xóa công trình thành công"));
    }
}
