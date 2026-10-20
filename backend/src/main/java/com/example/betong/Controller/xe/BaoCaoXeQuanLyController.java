package com.example.betong.Controller.xe;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.xe.BaoCaoTinhTrangXeResponse;
import com.example.betong.Service.xe.BaoCaoTinhTrangXeService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quan-ly/bao-cao-xe")
public class BaoCaoXeQuanLyController {
    private final BaoCaoTinhTrangXeService service;
    public BaoCaoXeQuanLyController(BaoCaoTinhTrangXeService service) { this.service = service; }
    @GetMapping
    public PageResponse<BaoCaoTinhTrangXeResponse> danhSach(
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSachBaoCao(trang, soLuong);
    }
}
