package com.example.betong.Controller.donhang;

import com.example.betong.DTO.request.donhang.DatBeTongRequest;
import com.example.betong.DTO.response.donhang.DatBeTongResponse;
import com.example.betong.Service.donhang.DatBeTongService;
import com.example.betong.Service.betong.LoaiBeTongService;
import com.example.betong.DTO.response.betong.LoaiBeTongResponse;
import com.example.betong.DTO.response.common.PageResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/khach-hang/dat-be-tong")
public class DatBeTongController {
    private final DatBeTongService service;
    private final LoaiBeTongService loaiBeTongService;

    public DatBeTongController(DatBeTongService service, LoaiBeTongService loaiBeTongService) {
        this.service = service;
        this.loaiBeTongService = loaiBeTongService;
    }

    @GetMapping("/loai-be-tong")
    public PageResponse<LoaiBeTongResponse> danhSachLoai(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return loaiBeTongService.danhSach(tuKhoa, trang, soLuong);
    }

    @PostMapping("/dat-hang")
    public DatBeTongResponse datHang(@Valid @RequestBody DatBeTongRequest request,
                                     Authentication authentication) {
        return service.datHang(authentication.getName(), request);
    }
}
