package com.example.betong.Controller.user;

import com.example.betong.DTO.request.user.CapNhatHoSoRequest;
import com.example.betong.DTO.request.user.DoiMatKhauRequest;
import com.example.betong.DTO.response.user.TaiKhoanResponse;
import com.example.betong.Service.user.HoSoCaNhanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.betong.DTO.response.auth.ThongBaoResponse;

/**
 * Bảng 3.39 - Cập nhật thông tin cá nhân, Bảng 3.40 - Đổi mật khẩu.
 * Dùng cho MỌI vai trò đã đăng nhập, chỉ thao tác trên hồ sơ của chính
 * mình (lấy từ Authentication, không nhận idTK từ client) — khác với
 * /api/quan-ly/tai-khoan chỉ Quản lý mới gọi được và thao tác trên tài
 * khoản của NGƯỜI KHÁC.
 */
@RestController
@RequestMapping("/api/nguoi-dung")
public class HoSoCaNhanController {

    private final HoSoCaNhanService service;

    public HoSoCaNhanController(HoSoCaNhanService service) {
        this.service = service;
    }

    @GetMapping("/ho-so")
    public TaiKhoanResponse xemHoSo(Authentication authentication) {
        return service.xemHoSo(authentication.getName());
    }

    @PutMapping("/ho-so")
    public TaiKhoanResponse capNhatHoSo(@Valid @RequestBody CapNhatHoSoRequest request,
                                        Authentication authentication) {
        return service.capNhatHoSo(authentication.getName(), request);
    }

    @PostMapping(value = "/ho-so/anh", consumes = "multipart/form-data")
    public TaiKhoanResponse capNhatAnh(@RequestPart("anh") MultipartFile anh,
                                       Authentication authentication) {
        return service.capNhatAnhDaiDien(authentication.getName(), anh);
    }

    @PutMapping("/doi-mat-khau")
    public ResponseEntity<ThongBaoResponse> doiMatKhau(@Valid @RequestBody DoiMatKhauRequest request,
                                                       Authentication authentication) {
        service.doiMatKhau(authentication.getName(), request);
        return ResponseEntity.ok(new ThongBaoResponse("Đổi mật khẩu thành công"));
    }
}
