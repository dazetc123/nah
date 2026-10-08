package com.example.betong.Service.taixechuyen;

import com.example.betong.Common.TrangThaiChuyen;
import com.example.betong.DTO.request.taixechuyen.DaDenCongTrinhRequest;
import com.example.betong.DTO.response.taixechuyen.ChuyenChiTietResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.file.FileStorageService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.Service.taixechuyen.impl.ChuyenTaiXeServiceImpl;
import com.example.betong.entity.*;
import com.example.betong.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Kiểm tra quy tắc chuyển trạng thái mục 2.2.1 / 2.2.3 (không cần CSDL). */
class ChuyenTaiXeServiceImplTest {

    private static final double VI_DO_CT = 21.0045, KINH_DO_CT = 105.7985;

    private ChuyenRepository chuyenRepo;
    private DonHangRepository donHangRepo;
    private ChuyenTaiXeServiceImpl service;
    private Chuyen chuyen;
    private DonHang donHang;

    @BeforeEach
    void setUp() {
        chuyenRepo = mock(ChuyenRepository.class);
        TaiXeRepository taiXeRepo = mock(TaiXeRepository.class);
        TaiKhoanRepository taiKhoanRepo = mock(TaiKhoanRepository.class);
        donHangRepo = mock(DonHangRepository.class);
        XeRepository xeRepo = mock(XeRepository.class);

        TaiKhoan tk = TaiKhoan.builder().idTK(1L).tenDangNhap("taixe01").build();
        TaiXe tx = TaiXe.builder().idTX(10L).hoTen("Tài xế 1").build();
        when(taiKhoanRepo.findByTenDangNhapOrEmail("taixe01")).thenReturn(Optional.of(tk));
        when(taiXeRepo.findByTaiKhoanIdTK(1L)).thenReturn(Optional.of(tx));

        CongTrinh ct = CongTrinh.builder().idCT(5L).tenCongTrinh("CT 1").viDo(VI_DO_CT).kinhDo(KINH_DO_CT).build();
        donHang = DonHang.builder().idDH(7L).congTrinh(ct).tongKhoiLuong(17.0).trangThai(3).chiTietDonHangs(List.of()).build();
        chuyen = Chuyen.builder().idChuyen(100L).donHang(donHang).taiXe(tx)
                .xe(Xe.builder().idXe(3L).bienSo("29C-123.45").build())
                .tramTron(TramTron.builder().idTram(2L).tenTram("Trạm 1").build())
                .khoiLuong(8.5).build();
        when(chuyenRepo.timCuaTaiXe(100L, 10L)).thenReturn(Optional.of(chuyen));
        when(chuyenRepo.saveAndFlush(any(Chuyen.class))).thenAnswer(i -> i.getArgument(0));
        when(chuyenRepo.tongKhoiLuongDaGiao(anyLong())).thenReturn(0.0);

        service = new ChuyenTaiXeServiceImpl(chuyenRepo, taiXeRepo, taiKhoanRepo, donHangRepo, xeRepo,
                mock(NotificationService.class), mock(FileStorageService.class));
    }

    private DaDenCongTrinhRequest req(Double viDo, Double kinhDo, String ghiChu) {
        DaDenCongTrinhRequest r = new DaDenCongTrinhRequest();
        r.setViDo(viDo); r.setKinhDo(kinhDo); r.setGhiChu(ghiChu);
        return r;
    }

    private HttpStatus loi(Runnable r) {
        return assertThrows(AppException.class, r::run).getStatus();
    }

    // ---------- Nhận chuyến ----------

    @Test
    void nhanChuyen_dangCoChuyenKhac_biChan() {
        chuyen.setTrangThai(TrangThaiChuyen.CHO_NHAN);
        when(chuyenRepo.taiXeCoChuyenKhacChuaXong(10L, 100L)).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT, loi(() -> service.nhanChuyen("taixe01", 100L)));
    }

    @Test
    void nhanChuyen_hopLe_luuThoiGianNhan() {
        chuyen.setTrangThai(TrangThaiChuyen.CHO_NHAN);
        ChuyenChiTietResponse r = service.nhanChuyen("taixe01", 100L);
        assertEquals(TrangThaiChuyen.DA_NHAN, r.getTrangThai());
        assertNotNull(r.getThoiGianNhan());
    }

    // ---------- Đã đến công trình ----------

    @Test
    void daDen_trongBanKinh_khongCanKiemTra() {
        chuyen.setTrangThai(TrangThaiChuyen.DANG_GIAO);
        ChuyenChiTietResponse r = service.xacNhanDaDen("taixe01", 100L, req(21.0050, 105.7990, null));
        assertEquals(TrangThaiChuyen.DA_DEN, r.getTrangThai());
        assertFalse(r.getCanKiemTraDen());
        assertTrue(r.getKhoangCachDen() < ChuyenTaiXeServiceImpl.BAN_KINH_CHO_PHEP_M);
        assertNotNull(r.getThoiGianDen());
    }

    @Test
    void daDen_ngoaiBanKinh_khongGhiChu_422_coGhiChu_canKiemTra() {
        chuyen.setTrangThai(TrangThaiChuyen.DANG_GIAO);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
                loi(() -> service.xacNhanDaDen("taixe01", 100L, req(21.03, 105.85, null))));
        ChuyenChiTietResponse r = service.xacNhanDaDen("taixe01", 100L, req(21.03, 105.85, "Cổng phụ"));
        assertEquals(TrangThaiChuyen.DA_DEN, r.getTrangThai());
        assertTrue(r.getCanKiemTraDen());
        assertEquals("Cổng phụ", r.getGhiChuDen());
    }

    @Test
    void daDen_khongCoViTri_batBuocGhiChu() {
        chuyen.setTrangThai(TrangThaiChuyen.DANG_GIAO);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
                loi(() -> service.xacNhanDaDen("taixe01", 100L, req(null, null, "  "))));
        ChuyenChiTietResponse r = service.xacNhanDaDen("taixe01", 100L, req(null, null, "Mất GPS"));
        assertTrue(r.getCanKiemTraDen());
    }

    @Test
    void daDen_saiTrangThai_409() {
        chuyen.setTrangThai(TrangThaiChuyen.DA_NHAN);
        assertEquals(HttpStatus.CONFLICT,
                loi(() -> service.xacNhanDaDen("taixe01", 100L, req(VI_DO_CT, KINH_DO_CT, null))));
    }

    // ---------- Xác nhận giao hàng ----------

    @Test
    void giaoHang_thieuKhoiLuong_400() {
        chuyen.setTrangThai(TrangThaiChuyen.DA_DEN);
        assertEquals(HttpStatus.BAD_REQUEST, loi(() -> service.xacNhanGiaoHang("taixe01", 100L, null, null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, loi(() -> service.xacNhanGiaoHang("taixe01", 100L, 0.0, null, null)));
    }

    @Test
    void giaoHang_vuotKhoiLuong_canGhiChu() {
        chuyen.setTrangThai(TrangThaiChuyen.DA_DEN);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
                loi(() -> service.xacNhanGiaoHang("taixe01", 100L, 9.0, "", null)));
        ChuyenChiTietResponse r = service.xacNhanGiaoHang("taixe01", 100L, 9.0, "Khách yêu cầu thêm", null);
        assertEquals(TrangThaiChuyen.DA_GIAO_HANG, r.getTrangThai());
        assertEquals(9.0, r.getKhoiLuongThucGiao());
    }

    @Test
    void giaoHang_hopLe() {
        chuyen.setTrangThai(TrangThaiChuyen.DA_DEN);
        ChuyenChiTietResponse r = service.xacNhanGiaoHang("taixe01", 100L, 8.5, null, null);
        assertEquals(TrangThaiChuyen.DA_GIAO_HANG, r.getTrangThai());
        assertNotNull(r.getThoiGianGiaoXong());
    }

    @Test
    void giaoHang_chuaDen_409() {
        chuyen.setTrangThai(TrangThaiChuyen.DANG_GIAO);
        assertEquals(HttpStatus.CONFLICT, loi(() -> service.xacNhanGiaoHang("taixe01", 100L, 8.5, null, null)));
    }

    // ---------- Hoàn thành chuyến ----------

    @Test
    void hoanThanh_chuaGiaoHang_409() {
        chuyen.setTrangThai(TrangThaiChuyen.DA_DEN);
        assertEquals(HttpStatus.CONFLICT, loi(() -> service.hoanThanhChuyen("taixe01", 100L)));
    }

    @Test
    void hoanThanh_donConChuyenKhac_khongDongDon() {
        chuyen.setTrangThai(TrangThaiChuyen.DA_GIAO_HANG);
        when(chuyenRepo.donHangConChuyenKhacChuaXong(7L, 100L)).thenReturn(true);
        ChuyenChiTietResponse r = service.hoanThanhChuyen("taixe01", 100L);
        assertEquals(TrangThaiChuyen.HOAN_THANH, r.getTrangThai());
        assertEquals(3, donHang.getTrangThai());
        verify(donHangRepo, never()).saveAndFlush(any());
    }

    @Test
    void hoanThanh_chuyenCuoi_dongDon() {
        chuyen.setTrangThai(TrangThaiChuyen.DA_GIAO_HANG);
        ChuyenChiTietResponse r = service.hoanThanhChuyen("taixe01", 100L);
        assertNotNull(r.getThoiGianHoanThanh());
        assertEquals(4, donHang.getTrangThai());
    }
}
