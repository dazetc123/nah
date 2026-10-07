package com.example.betong.Service.taixe.impl;

import com.example.betong.DTO.request.taixe.SuaTaiXeRequest;
import com.example.betong.DTO.request.taixe.ThemTaiXeRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.taixe.TaiXeResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.taixe.TaiXeService;
import com.example.betong.entity.TaiKhoan;
import com.example.betong.entity.TaiXe;
import com.example.betong.entity.Xe;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.TaiKhoanRepository;
import com.example.betong.repository.TaiXeRepository;
import com.example.betong.repository.XeRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bám sát Bảng 3.23 (Thêm), 3.24 (Xóa), 3.25 (Sửa), 3.26 (Tìm kiếm) -
 * Tác nhân duy nhất: Quản lý. Bảng 3.27 (Gán tài xế cho xe) đã có sẵn ở
 * XeService (endpoint /api/quan-ly/xe/{id}/tai-xe), không lặp lại ở đây.
 */
@Service
public class TaiXeServiceImpl implements TaiXeService {

    private final TaiXeRepository taiXeRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final XeRepository xeRepository;
    private final ChuyenRepository chuyenRepository;

    public TaiXeServiceImpl(TaiXeRepository taiXeRepository,
                             TaiKhoanRepository taiKhoanRepository,
                             XeRepository xeRepository,
                             ChuyenRepository chuyenRepository) {
        this.taiXeRepository = taiXeRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.xeRepository = xeRepository;
        this.chuyenRepository = chuyenRepository;
    }

    // ================================================================
    // Bảng 3.26 - Tìm kiếm (kiêm xem danh sách)
    // 4.a "không có tài xế phù hợp" khi rỗng kết quả xử lý ở Frontend
    // (empty-state), giống hệt cách Xe/TramTron đang làm.
    // ================================================================
    @Override
    public PageResponse<TaiXeResponse> danhSach(String tuKhoa, int trang, int soLuong) {
        String tuKhoaThucSu = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong));
        return PageResponse.tu(taiXeRepository.timKiem(tuKhoaThucSu, pageable), this::sangResponse);
    }

    @Override
    public TaiXeResponse layChiTiet(Long idTX) {
        return sangResponse(timHoacBaoLoi(idTX));
    }

    // ================================================================
    // Bảng 3.23 - Thêm tài xế
    // ================================================================
    @Override
    @Transactional
    public TaiXeResponse them(ThemTaiXeRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(request.getIdTK())
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản", HttpStatus.NOT_FOUND));

        if (!"Tài xế".equals(taiKhoan.getVaiTro().getTenVaiTro())) {
            throw new AppException("Tài khoản được chọn không mang vai trò Tài xế", HttpStatus.BAD_REQUEST);
        }
        if (taiXeRepository.findByTaiKhoanIdTK(request.getIdTK()).isPresent()) {
            throw new AppException("Tài khoản này đã có hồ sơ tài xế rồi", HttpStatus.CONFLICT);
        }
        if (taiXeRepository.existsBySoGPLXIgnoreCase(request.getSoGPLX().trim())) {
            throw new AppException("Số giấy phép lái xe đã tồn tại trong hệ thống", HttpStatus.CONFLICT);
        }

        TaiXe taiXe = TaiXe.builder()
                .taiKhoan(taiKhoan)
                .hoTen(request.getHoTen().trim())
                .soGPLX(request.getSoGPLX().trim())
                .sdt(request.getSdt())
                .trangThai(1)
                .build();
        taiKhoan.setHoTen(taiXe.getHoTen());
        taiKhoan.setSdt(taiXe.getSdt());
        taiKhoanRepository.save(taiKhoan);

        try {
            return sangResponse(taiXeRepository.save(taiXe), "Thêm tài xế thành công");
        } catch (DataAccessException ex) {
            // 5.a: "Nếu hệ thống bị lỗi hoặc mất kết nối"
            throw new AppException("Thêm tài xế thất bại, yêu cầu thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Bảng 3.25 - Sửa tài xế (API tương thích, đã khóa)
    // ================================================================
    @Override
    @Transactional
    public TaiXeResponse sua(Long idTX, SuaTaiXeRequest request) {
        TaiXe taiXe = timHoacBaoLoi(idTX);

        throw new AppException(
                "Quản lý không được sửa hồ sơ tài xế. Tài xế tự cập nhật thông tin trong hồ sơ cá nhân",
                HttpStatus.FORBIDDEN);
    }

    // ================================================================
    // Bảng 3.24 - Xóa tài xế
    // PHƯƠNG ÁN ĐÃ CHỌN: chỉ xóa hồ sơ TaiXe, GIỮ NGUYÊN tài khoản đăng
    // nhập (TaiKhoan không bị đụng tới). Trước khi xóa: tự động hủy gán
    // mọi xe đang gán cho tài xế này (tránh vi phạm khóa ngoại Xe.taiXe);
    // chặn xóa nếu tài xế đã có lịch sử Chuyến (tương ứng nhánh phụ báo lỗi).
    // ================================================================
    @Override
    @Transactional
    public void xoa(Long idTX) {
        TaiXe taiXe = timHoacBaoLoi(idTX);

        if (chuyenRepository.existsByTaiXe_IdTX(idTX)) {
            throw new AppException("Không thể xóa tài xế vì đang có dữ liệu chuyến liên quan", HttpStatus.CONFLICT);
        }

        try {
            List<Xe> xeDangGan = xeRepository.findByTaiXe_IdTX(idTX);
            for (Xe xe : xeDangGan) {
                xe.setTaiXe(null);
            }
            xeRepository.saveAll(xeDangGan);

            taiXeRepository.delete(taiXe);
        } catch (DataAccessException ex) {
            // 4.a: "Nếu hệ thống có lỗi không thể xóa"
            throw new AppException("Có lỗi, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Hàm hỗ trợ nội bộ
    // ================================================================
    private TaiXe timHoacBaoLoi(Long idTX) {
        return taiXeRepository.findById(idTX)
                .orElseThrow(() -> new AppException("Không tìm thấy tài xế", HttpStatus.NOT_FOUND));
    }

    private TaiXeResponse sangResponse(TaiXe t) {
        return sangResponse(t, null);
    }

    private TaiXeResponse sangResponse(TaiXe t, String thongBao) {
        String bienSo = xeRepository.findByTaiXe_IdTX(t.getIdTX()).stream()
                .findFirst().map(Xe::getBienSo).orElse(null);

        return TaiXeResponse.builder()
                .idTX(t.getIdTX())
                .idTK(t.getTaiKhoan().getIdTK())
                .tenDangNhap(t.getTaiKhoan().getTenDangNhap())
                .hoTen(t.getHoTen())
                .anhDaiDien(t.getTaiKhoan().getAnhDaiDien())
                .ngaySinh(t.getTaiKhoan().getNgaySinh())
                .diaChiThuongTru(t.getTaiKhoan().getDiaChiThuongTru())
                .gioiTinh(t.getTaiKhoan().getGioiTinh())
                .soGPLX(t.getSoGPLX())
                .sdt(t.getSdt())
                .trangThai(t.getTrangThai())
                .bienSoXeDangGan(bienSo)
                .thongBao(thongBao)
                .build();
    }
}
