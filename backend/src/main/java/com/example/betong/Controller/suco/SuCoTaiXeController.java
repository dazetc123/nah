package com.example.betong.Controller.suco;

import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.suco.SuCoResponse;
import com.example.betong.Service.suco.SuCoTaiXeService;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mục 2.2.3 báo cáo - Usecase "Báo cáo sự cố" (tác nhân Tài xế).
 * Nằm dưới /api/tai-xe/** nên SecurityConfig đã giới hạn vai trò Tài xế.
 */
@RestController
@RequestMapping("/api/tai-xe/su-co")
public class SuCoTaiXeController {

    private final SuCoTaiXeService service;

    public SuCoTaiXeController(SuCoTaiXeService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SuCoResponse baoCao(@RequestParam(value = "idChuyen", required = false) Long idChuyen,
                               @RequestParam(value = "loaiSuCo", required = false) String loaiSuCo,
                               @RequestParam(value = "moTa", required = false) String moTa,
                               @RequestParam(value = "mucDoUuTien", required = false) Integer mucDoUuTien,
                               @RequestParam(value = "viDo", required = false) Double viDo,
                               @RequestParam(value = "kinhDo", required = false) Double kinhDo,
                               @RequestPart(value = "anh", required = false) MultipartFile anh,
                               Authentication authentication) {
        return service.baoCaoSuCo(authentication.getName(), idChuyen, loaiSuCo, moTa, mucDoUuTien, viDo, kinhDo, anh);
    }

    @GetMapping
    public PageResponse<SuCoResponse> danhSach(@RequestParam(defaultValue = "1") @Min(1) int trang,
                                               @RequestParam(defaultValue = "20") @Min(1) int soLuong,
                                               Authentication authentication) {
        return service.danhSachSuCoCuaToi(authentication.getName(), trang, soLuong);
    }
}
