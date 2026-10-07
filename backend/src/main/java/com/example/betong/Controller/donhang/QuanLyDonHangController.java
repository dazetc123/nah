package com.example.betong.Controller.donhang;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.donhang.DonHangResponse;
import com.example.betong.Service.donhang.QuanLyDonHangService;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/khach-hang/don-hang")
public class QuanLyDonHangController {
    private final QuanLyDonHangService service;
    public QuanLyDonHangController(QuanLyDonHangService service) { this.service = service; }

    @GetMapping
    public PageResponse<DonHangResponse> danhSach(Authentication auth, @RequestParam(required = false) String tuKhoa,
                                                   @RequestParam(defaultValue = "1") @Min(1) int trang,
                                                   @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSach(auth.getName(), tuKhoa, trang, soLuong);
    }
    @GetMapping("/{idDH}") public DonHangResponse chiTiet(Authentication a, @PathVariable Long idDH) { return service.chiTiet(a.getName(), idDH); }
    @GetMapping("/{idDH}/theo-doi") public DonHangResponse theoDoi(Authentication a, @PathVariable Long idDH) { return service.theoDoi(a.getName(), idDH); }
    @PutMapping("/{idDH}/huy") public DonHangResponse huy(Authentication a, @PathVariable Long idDH) { return service.huy(a.getName(), idDH); }
}
