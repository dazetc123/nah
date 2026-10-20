package com.example.betong.Controller.dieuphoi;

import com.example.betong.DTO.request.dieuphoi.TaoChuyenRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.dieuphoi.ChuyenResponse;
import com.example.betong.DTO.response.donhang.TramTronKhaDungResponse;
import com.example.betong.DTO.response.taixe.TaiXeResponse;
import com.example.betong.DTO.response.xe.XeResponse;
import com.example.betong.Service.dieuphoi.DieuPhoiXeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dieu-phoi/xe")
public class DieuPhoiXeController {
    private final DieuPhoiXeService service;
    public DieuPhoiXeController(DieuPhoiXeService service) { this.service = service; }

    @GetMapping
    public PageResponse<XeResponse> timXe(@RequestParam(required = false) String tuKhoa,
                                          @RequestParam(defaultValue = "1") @Min(1) int trang,
                                          @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.timXeRanh(tuKhoa, trang, soLuong);
    }
    @GetMapping("/tai-xe")
    public PageResponse<TaiXeResponse> timTaiXe(@RequestParam(required = false) String tuKhoa,
                                                @RequestParam(defaultValue = "1") @Min(1) int trang,
                                                @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.timTaiXeRanh(tuKhoa, trang, soLuong);
    }
    @GetMapping("/don-hang/{idDH}/tram-tron")
    public List<TramTronKhaDungResponse> tramTron(@PathVariable Long idDH) {
        return service.tramTronKhaDung(idDH);
    }
    @PostMapping("/chuyen")
    public ChuyenResponse taoChuyen(@Valid @RequestBody TaoChuyenRequest request) {
        return service.taoChuyen(request);
    }
}
