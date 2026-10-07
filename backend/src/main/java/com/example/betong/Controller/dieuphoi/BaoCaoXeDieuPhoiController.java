package com.example.betong.Controller.dieuphoi;

import com.example.betong.DTO.response.xe.BaoCaoTinhTrangXeResponse;
import com.example.betong.Service.xe.BaoCaoTinhTrangXeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/dieu-phoi/xe")
public class BaoCaoXeDieuPhoiController {
    private final BaoCaoTinhTrangXeService service;
    public BaoCaoXeDieuPhoiController(BaoCaoTinhTrangXeService service) { this.service = service; }
    @PostMapping(value = "/{id}/bao-cao", consumes = "multipart/form-data")
    public BaoCaoTinhTrangXeResponse taoBaoCao(@PathVariable Long id,
            @RequestParam("trangThaiXe") Integer trangThaiXe,
            @RequestParam("diaChiHu") String diaChiHu,
            @RequestParam("soDienThoaiTaiXe") String soDienThoaiTaiXe,
            @RequestParam("nguyenNhan") String nguyenNhan,
            @RequestParam("noiDung") String noiDung,
            @RequestPart("anh") MultipartFile anh, Authentication authentication) {
        return service.taoBaoCao(id, authentication.getName(), trangThaiXe, diaChiHu,
                soDienThoaiTaiXe, nguyenNhan, noiDung, anh);
    }
}
