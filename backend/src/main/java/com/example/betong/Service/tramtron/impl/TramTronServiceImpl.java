package com.example.betong.Service.tramtron.impl;

import com.example.betong.DTO.request.tramtron.CongSuatRequest;
import com.example.betong.DTO.request.tramtron.TramTronRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.tramtron.TramTronResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.tramtron.TramTronService;
import com.example.betong.entity.TramTron;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.TramTronRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bám sát Bảng 3.18 (Thêm), 3.19 (Xóa), 3.20 (Sửa), 3.21 (Tìm kiếm),
 * 3.22 (Quản lý công suất) - Tác nhân duy nhất: Quản lý. Cùng style xử
 * lý lỗi/thông báo với XeServiceImpl để nhất quán trong toàn hệ thống.
 */
@Service
public class TramTronServiceImpl implements TramTronService {

    private final TramTronRepository tramTronRepository;
    private final ChuyenRepository chuyenRepository;

    public TramTronServiceImpl(TramTronRepository tramTronRepository, ChuyenRepository chuyenRepository) {
        this.tramTronRepository = tramTronRepository;
        this.chuyenRepository = chuyenRepository;
    }

    // ================================================================
    // Bảng 3.21 - Tìm kiếm (kiêm xem danh sách khi tuKhoa rỗng)
    // 4.a "không có trạm trộn phù hợp" khi có tìm kiếm nhưng rỗng kết quả
    // được xử lý ở tầng Frontend (empty-state), không throw lỗi ở đây -
    // giống hệt cách XeServiceImpl.danhSach() đang làm.
    // ================================================================
    @Override
    public PageResponse<TramTronResponse> danhSach(String tuKhoa, int trang, int soLuong) {
        String tuKhoaThucSu = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong));
        return PageResponse.tu(tramTronRepository.timKiem(tuKhoaThucSu, pageable), this::sangResponse);
    }

    @Override
    public TramTronResponse layChiTiet(Long idTram) {
        return sangResponse(timHoacBaoLoi(idTram));
    }

    // ================================================================
    // Bảng 3.18 - Thêm trạm trộn
    // ================================================================
    @Override
    @Transactional
    public TramTronResponse them(TramTronRequest request) {
        if (tramTronRepository.existsByTenTramIgnoreCase(request.getTenTram().trim())) {
            throw new AppException("Tên trạm trộn đã tồn tại, vui lòng chọn tên khác", HttpStatus.CONFLICT);
        }

        TramTron tramTron = TramTron.builder()
                .tenTram(request.getTenTram().trim())
                .diaChi(request.getDiaChi().trim())
                .congSuat(request.getCongSuat())
                .sdt(request.getSdt())
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : 1)
                .build();

        try {
            return sangResponse(tramTronRepository.save(tramTron), "Thêm trạm trộn thành công");
        } catch (DataAccessException ex) {
            // 5.a: "Nếu hệ thống bị lỗi hoặc mất kết nối"
            throw new AppException("Thêm trạm trộn thất bại, yêu cầu thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Bảng 3.20 - Sửa trạm trộn
    // ================================================================
    @Override
    @Transactional
    public TramTronResponse sua(Long idTram, TramTronRequest request) {
        TramTron tramTron = timHoacBaoLoi(idTram);

        if (tramTronRepository.existsByTenTramIgnoreCaseAndIdTramNot(request.getTenTram().trim(), idTram)) {
            throw new AppException("Tên trạm trộn đã tồn tại, vui lòng chọn tên khác", HttpStatus.CONFLICT);
        }

        tramTron.setTenTram(request.getTenTram().trim());
        tramTron.setDiaChi(request.getDiaChi().trim());
        tramTron.setCongSuat(request.getCongSuat());
        tramTron.setSdt(request.getSdt());
        if (request.getTrangThai() != null) {
            tramTron.setTrangThai(request.getTrangThai());
        }

        try {
            return sangResponse(tramTronRepository.save(tramTron), "Sửa trạm trộn thành công");
        } catch (DataAccessException ex) {
            // 5.a: "Nếu hệ thống bị lỗi hoặc mất kết nối"
            throw new AppException("Cập nhật trạm trộn thất bại, yêu cầu thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Bảng 3.19 - Xóa trạm trộn
    // (bước 2-3 "hiển thị hộp thoại xác nhận" thuộc Frontend; API này xử
    //  lý từ lúc người dùng đã bấm đồng ý)
    // ================================================================
    @Override
    @Transactional
    public void xoa(Long idTram) {
        TramTron tramTron = timHoacBaoLoi(idTram);
        if (chuyenRepository.existsByTramTron_IdTram(idTram)) {
            throw new AppException("Không thể xóa trạm trộn vì đang có dữ liệu liên quan", HttpStatus.CONFLICT);
        }
        try {
            tramTronRepository.delete(tramTron);
        } catch (DataAccessException ex) {
            // 4.a: "Nếu hệ thống có lỗi không thể xóa"
            throw new AppException("Có lỗi, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Bảng 3.22 - Quản lý công suất trạm trộn (usecase riêng, chỉ đổi công suất)
    // ================================================================
    @Override
    @Transactional
    public TramTronResponse capNhatCongSuat(Long idTram, CongSuatRequest request) {
        TramTron tramTron = timHoacBaoLoi(idTram);
        tramTron.setCongSuat(request.getCongSuat());
        try {
            return sangResponse(tramTronRepository.save(tramTron), "Chỉnh sửa công suất thành công");
        } catch (DataAccessException ex) {
            throw new AppException("Cập nhật công suất thất bại, yêu cầu thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Hàm hỗ trợ nội bộ
    // ================================================================
    private TramTron timHoacBaoLoi(Long idTram) {
        return tramTronRepository.findById(idTram)
                .orElseThrow(() -> new AppException("Không tìm thấy trạm trộn", HttpStatus.NOT_FOUND));
    }

    private TramTronResponse sangResponse(TramTron t) {
        return sangResponse(t, null);
    }

    private TramTronResponse sangResponse(TramTron t, String thongBao) {
        return TramTronResponse.builder()
                .idTram(t.getIdTram())
                .tenTram(t.getTenTram())
                .diaChi(t.getDiaChi())
                .congSuat(t.getCongSuat())
                .sdt(t.getSdt())
                .trangThai(t.getTrangThai())
                .thongBao(thongBao)
                .build();
    }
}
