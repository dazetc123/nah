package com.example.betong.Controller.taixechuyen;

import com.example.betong.Exception.AppException;
import com.example.betong.entity.TaiKhoan;
import com.example.betong.entity.TaiXe;
import com.example.betong.entity.Xe;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.TaiKhoanRepository;
import com.example.betong.repository.TaiXeRepository;
import com.example.betong.repository.XeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Xe đang được gán cho tài xế đăng nhập, dùng cho màn "Trạng thái xe" trên app:
 * biết báo cáo cho xe nào, trạng thái hiện tại, và xe có đang chạy chuyến không.
 */
@RestController
@RequestMapping("/api/tai-xe/xe-cua-toi")
public class XeCuaToiController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiXeRepository taiXeRepository;
    private final XeRepository xeRepository;
    private final ChuyenRepository chuyenRepository;

    public XeCuaToiController(TaiKhoanRepository taiKhoanRepository, TaiXeRepository taiXeRepository,
                              XeRepository xeRepository, ChuyenRepository chuyenRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.taiXeRepository = taiXeRepository;
        this.xeRepository = xeRepository;
        this.chuyenRepository = chuyenRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, Object> xeCuaToi(Authentication authentication) {
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhapOrEmail(authentication.getName())
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));
        TaiXe tx = taiXeRepository.findByTaiKhoanIdTK(tk.getIdTK())
                .orElseThrow(() -> new AppException("Tài khoản chưa có hồ sơ tài xế", HttpStatus.FORBIDDEN));
        List<Xe> xes = xeRepository.findByTaiXe_IdTX(tx.getIdTX());
        if (xes.isEmpty()) {
            throw new AppException("Bạn chưa được gán xe nào, vui lòng liên hệ quản lý", HttpStatus.NOT_FOUND);
        }
        Xe xe = xes.get(0);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("idXe", xe.getIdXe());
        res.put("bienSo", xe.getBienSo());
        res.put("trongTai", xe.getTrongTai());
        res.put("trangThai", xe.getTrangThai());
        res.put("tenTrangThai", Integer.valueOf(1).equals(xe.getTrangThai()) ? "Đang hoạt động" : "Bảo trì");
        res.put("dangCoChuyen", chuyenRepository.xeDangBan(xe.getIdXe()));
        return res;
    }
}
