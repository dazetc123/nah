package com.example.betong.Controller.dieuphoi;

import com.example.betong.DTO.request.donhang.CapNhatTrangThaiDonHangRequest;
import com.example.betong.DTO.request.donhang.TuChoiDonHangRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.donhang.DonHangResponse;
import com.example.betong.DTO.response.donhang.TramTronKhaDungResponse;
import com.example.betong.Service.donhang.DieuPhoiDonHangService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dieu-phoi/don-hang")
public class DieuPhoiDonHangController {
    private final DieuPhoiDonHangService service;

    public DieuPhoiDonHangController(DieuPhoiDonHangService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<DonHangResponse> danhSach(@RequestParam(defaultValue = "1") @Min(1) int trang,
                                                   @RequestParam(defaultValue = "20") @Min(1) int soLuong) {
        return service.danhSachChoXuLy(trang, soLuong);
    }

    @GetMapping("/{idDH}")
    public DonHangResponse chiTiet(@PathVariable Long idDH) { return service.chiTiet(idDH); }

    @PutMapping("/{idDH}/xac-nhan")
    public DonHangResponse xacNhan(@PathVariable Long idDH) { return service.xacNhan(idDH); }

    @PutMapping("/{idDH}/tu-choi")
    public DonHangResponse tuChoi(@PathVariable Long idDH, @Valid @RequestBody TuChoiDonHangRequest request) {
        return service.tuChoi(idDH, request);
    }

    @GetMapping("/{idDH}/tram-tron-kha-dung")
    public List<TramTronKhaDungResponse> tramKhaDung(@PathVariable Long idDH) {
        return service.tramKhaDung(idDH);
    }

    @PutMapping("/{idDH}/phan-bo-tram-tron/{idTram}")
    public DonHangResponse phanBoTram(@PathVariable Long idDH, @PathVariable Long idTram) {
        return service.phanBoTram(idDH, idTram);
    }

    @PutMapping("/{idDH}/trang-thai")
    public DonHangResponse capNhatTrangThai(@PathVariable Long idDH,
                                             @Valid @RequestBody CapNhatTrangThaiDonHangRequest request) {
        return service.capNhatTrangThai(idDH, request);
    }
}
