package com.example.betong.Service.xe.impl;

import com.example.betong.DTO.request.xe.XeRequest;
import com.example.betong.DTO.response.common.PageResponse;
import com.example.betong.DTO.response.xe.XeResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.xe.XeService;
import com.example.betong.entity.TaiXe;
import com.example.betong.entity.Xe;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.TaiXeRepository;
import com.example.betong.repository.ViTriGPSRepository;
import com.example.betong.repository.XeRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class XeServiceImpl implements XeService {

    private static final int DANG_HOAT_DONG = 1;
    private static final int BAO_TRI = 0;

    private final XeRepository xeRepository;
    private final TaiXeRepository taiXeRepository;
    private final ChuyenRepository chuyenRepository;
    private final ViTriGPSRepository viTriGPSRepository;

    public XeServiceImpl(XeRepository xeRepository, TaiXeRepository taiXeRepository,
                         ChuyenRepository chuyenRepository, ViTriGPSRepository viTriGPSRepository) {
        this.xeRepository = xeRepository;
        this.taiXeRepository = taiXeRepository;
        this.chuyenRepository = chuyenRepository;
        this.viTriGPSRepository = viTriGPSRepository;
    }

    @Override
    public PageResponse<XeResponse> danhSach(String tuKhoa, Integer trangThai, int trang, int soLuong) {
        Pageable pageable = PageRequest.of(Math.max(0, trang - 1), Math.max(1, soLuong));
        String bienSo = null;
        Integer trangThaiLoc = trangThai;
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            String tuKhoaChuanHoa = tuKhoa.trim();
            Integer trangThaiTuKhoa = parseTrangThai(tuKhoaChuanHoa);
            if (trangThaiTuKhoa != null && trangThai == null) {
                trangThaiLoc = trangThaiTuKhoa;
            } else if (!tuKhoaChuanHoa.matches("^[0-9A-Za-z -]+$")) {
                return PageResponse.tu(org.springframework.data.domain.Page.<Xe>empty(pageable),
                        xe -> sangResponse(xe));
            } else {
                bienSo = tuKhoaChuanHoa;
            }
        }
        // Bộ lọc danh sách không hợp lệ không phải là lỗi nghiệp vụ của thao tác xe.
        // Trả về danh sách rỗng để giao diện hiển thị "Chưa có xe phù hợp".
        if (trangThaiLoc != null && !laTrangThaiHopLe(trangThaiLoc)) {
            return PageResponse.tu(org.springframework.data.domain.Page.<Xe>empty(pageable),
                    xe -> sangResponse(xe));
        }
        return PageResponse.tu(xeRepository.timKiem(bienSo, trangThaiLoc, pageable),
                xe -> sangResponse(xe));
    }

    @Override
    public XeResponse xemChiTiet(Long idXe) {
        return sangResponse(timXe(idXe));
    }

    @Override
    @Transactional
    public XeResponse them(XeRequest request) {
        validateTrangThai(request.getTrangThai());
        String bienSo = chuanHoaBienSo(request.getBienSo());
        if (xeRepository.existsByBienSoIgnoreCase(bienSo)) {
            throw new AppException("Biển số xe đã tồn tại", HttpStatus.CONFLICT);
        }
        Xe xe = new Xe(null, bienSo, request.getTrongTai(), request.getTrangThai(), null, null, null);
        try {
            return sangResponse(xeRepository.save(xe), "Thêm xe thành công");
        } catch (DataAccessException ex) {
            throw new AppException("Thêm thất bại, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public XeResponse sua(Long idXe, XeRequest request) {
        throw new AppException(
                "Quản lý chỉ được cập nhật trạng thái xe qua API /trang-thai",
                HttpStatus.FORBIDDEN);
    }

    @Override
    @Transactional
    public void xoa(Long idXe) {
        Xe xe = timXe(idXe);
        if (chuyenRepository.existsByXe_IdXe(idXe) || viTriGPSRepository.existsByXe_IdXe(idXe)) {
            throw new AppException("Không thể xóa xe vì xe đang có dữ liệu liên quan", HttpStatus.CONFLICT);
        }
        try {
            xeRepository.delete(xe);
        } catch (DataAccessException ex) {
            throw new AppException("Xóa xe thất bại, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public XeResponse capNhatTrangThai(Long idXe, Integer trangThai) {
        validateTrangThai(trangThai);
        Xe xe = timXe(idXe);
        xe.setTrangThai(trangThai);
        try {
            return sangResponse(xeRepository.save(xe), "Cập nhật trạng thái xe thành công");
        } catch (DataAccessException ex) {
            throw new AppException("Cập nhật trạng thái thất bại, yêu cầu thử lại sau",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public XeResponse ganTaiXe(Long idXe, Long idTX) {
        if (idTX == null) {
            throw new AppException("Tài xế không được để trống khi gán cho xe", HttpStatus.BAD_REQUEST);
        }
        Xe xe = timXe(idXe);
        TaiXe taiXe = timTaiXe(idTX);

        // Bảng 3.27 - 4.a: "Tài xế không phù hợp với xe, hệ thống báo lỗi, quay về bước 3"
        // Điều kiện "phù hợp": tài xế đang hoạt động và chưa lái xe nào khác cùng lúc.
        if (taiXe.getTrangThai() == null || taiXe.getTrangThai() != 1) {
            throw new AppException("Tài xế không phù hợp với xe: tài xế đang ngừng hoạt động", HttpStatus.BAD_REQUEST);
        }
        boolean dangLaiXeKhac = xeRepository.findByTaiXe_IdTX(idTX).stream()
                .anyMatch(x -> !x.getIdXe().equals(idXe));
        if (dangLaiXeKhac) {
            throw new AppException("Tài xế không phù hợp với xe: tài xế đang được gán cho một xe khác", HttpStatus.BAD_REQUEST);
        }

        xe.setTaiXe(taiXe);
        return sangResponse(xeRepository.save(xe), "Gán tài xế cho xe thành công");
    }

    @Override
    @Transactional
    public XeResponse huyGanTaiXe(Long idXe) {
        Xe xe = timXe(idXe);
        xe.setTaiXe(null);
        return sangResponse(xeRepository.save(xe), "Hủy gán tài xế cho xe thành công");
    }

    private Xe timXe(Long idXe) {
        return xeRepository.findById(idXe)
                .orElseThrow(() -> new AppException("Không tìm thấy xe", HttpStatus.NOT_FOUND));
    }

    private TaiXe timTaiXe(Long idTX) {
        return taiXeRepository.findById(idTX)
                .orElseThrow(() -> new AppException("Không tìm thấy tài xế", HttpStatus.BAD_REQUEST));
    }

    private void validateTrangThai(Integer trangThai) {
        if (!laTrangThaiHopLe(trangThai)) {
            throw new AppException("Trạng thái xe chỉ được là đang hoạt động (1) hoặc bảo trì (0)",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private boolean laTrangThaiHopLe(Integer trangThai) {
        return trangThai != null && (trangThai == DANG_HOAT_DONG || trangThai == BAO_TRI);
    }

    private Integer parseTrangThai(String value) {
        if ("1".equals(value) || "đang hoạt động".equalsIgnoreCase(value)
                || "hoạt động".equalsIgnoreCase(value)) {
            return DANG_HOAT_DONG;
        }
        if ("0".equals(value) || "bảo trì".equalsIgnoreCase(value)) {
            return BAO_TRI;
        }
        return null;
    }

    private String chuanHoaBienSo(String bienSo) {
        return bienSo.trim().toUpperCase();
    }

    private XeResponse sangResponse(Xe xe) {
        return sangResponse(xe, null);
    }

    private XeResponse sangResponse(Xe xe, String thongBao) {
        return XeResponse.builder()
                .idXe(xe.getIdXe())
                .bienSo(xe.getBienSo())
                .trongTai(xe.getTrongTai())
                .trangThai(xe.getTrangThai())
                .idTX(xe.getTaiXe() == null ? null : xe.getTaiXe().getIdTX())
                .thongBao(thongBao)
                .build();
    }
}
