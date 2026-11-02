package com.example.betong.Controller.dieuphoi;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.dieuphoi.TheoDoiXeResponse;
import com.example.betong.Service.dieuphoi.TheoDoiXeService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dieu-phoi/theo-doi-xe")
public class TheoDoiXeController {
    private final TheoDoiXeService service;

    public TheoDoiXeController(TheoDoiXeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<TheoDoiXeResponse> danhSach(
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSachXeDangHoatDong(trang, soLuong);
    }

    @GetMapping("/{idChuyen}/vi-tri")
    public TheoDoiXeResponse viTri(@PathVariable Long idChuyen) {
        return service.viTriMoiNhat(idChuyen);
    }

    @GetMapping("/{idChuyen}/trang-thai")
    public TheoDoiXeResponse trangThai(@PathVariable Long idChuyen) {
        return service.trangThaiChuyen(idChuyen);
    }
}
