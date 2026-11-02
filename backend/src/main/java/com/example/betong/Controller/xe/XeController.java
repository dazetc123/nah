package com.example.betong.Controller.xe;

import com.example.betong.DTO.request.xe.XeRequest;
import com.example.betong.DTO.request.xe.GanTaiXeRequest;
import com.example.betong.DTO.request.user.TrangThaiRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.auth.ThongBaoResponse;
import com.example.betong.DTO.response.xe.XeResponse;
import com.example.betong.Service.xe.XeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quan-ly/xe")
public class XeController {

    private final XeService service;

    public XeController(XeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<XeResponse> danhSach(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "1") @Min(1) int trang,
            @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSach(tuKhoa, trangThai, trang, soLuong);
    }

    @GetMapping("/{id}")
    public XeResponse xemChiTiet(@PathVariable Long id) {
        return service.xemChiTiet(id);
    }

    @PostMapping
    public ResponseEntity<XeResponse> them(@Valid @RequestBody XeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.them(request));
    }

    @PutMapping("/{id}")
    public XeResponse sua(@PathVariable Long id, @Valid @RequestBody XeRequest request) {
        return service.sua(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ThongBaoResponse> xoa(@PathVariable Long id) {
        service.xoa(id);
        return ResponseEntity.ok(new ThongBaoResponse("Xóa xe thành công"));
    }

    @PutMapping("/{id}/trang-thai")
    public XeResponse capNhatTrangThai(@PathVariable Long id,
                                       @Valid @RequestBody TrangThaiRequest request) {
        return service.capNhatTrangThai(id, request.getTrangThai());
    }

    @PutMapping("/{id}/tai-xe")
    public XeResponse ganTaiXe(@PathVariable Long id,
                               @RequestBody GanTaiXeRequest request) {
        return service.ganTaiXe(id, request.getIdTX());
    }

    @DeleteMapping("/{id}/tai-xe")
    public XeResponse huyGanTaiXe(@PathVariable Long id) {
        return service.huyGanTaiXe(id);
    }
}
