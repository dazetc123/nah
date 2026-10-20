package com.example.betong.Service.suco;

import com.example.betong.DTO.response.suco.SuCoResponse;
import com.example.betong.Exception.AppException;
import com.example.betong.Service.file.FileStorageService;
import com.example.betong.Service.notification.NotificationService;
import com.example.betong.Service.suco.impl.SuCoTaiXeServiceImpl;
import com.example.betong.entity.*;
import com.example.betong.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Kiểm tra usecase "Báo cáo sự cố" mục 2.2.3 (không cần CSDL). */
class SuCoTaiXeServiceImplTest {

    private ChuyenRepository chuyenRepo;
    private NotificationService notification;
    private SuCoTaiXeServiceImpl service;
    private Chuyen chuyen;

    @BeforeEach
    void setUp() {
        chuyenRepo = mock(ChuyenRepository.class);
        SuCoRepository suCoRepo = mock(SuCoRepository.class);
        TaiKhoanRepository taiKhoanRepo = mock(TaiKhoanRepository.class);
        TaiXeRepository taiXeRepo = mock(TaiXeRepository.class);
        notification = mock(NotificationService.class);

        when(taiKhoanRepo.findByTenDangNhapOrEmail("taixe01"))
                .thenReturn(Optional.of(TaiKhoan.builder().idTK(1L).tenDangNhap("taixe01").hoTen("A").build()));
        TaiXe tx = TaiXe.builder().idTX(10L).build();
        when(taiXeRepo.findByTaiKhoanIdTK(1L)).thenReturn(Optional.of(tx));
        chuyen = Chuyen.builder().idChuyen(100L).taiXe(tx).trangThai(2)
                .xe(Xe.builder().idXe(3L).bienSo("29C-123.45").build()).build();
        when(chuyenRepo.timCuaTaiXe(100L, 10L)).thenReturn(Optional.of(chuyen));
        when(suCoRepo.save(any(SuCo.class))).thenAnswer(i -> {
            SuCo s = i.getArgument(0);
            s.setIdSuCo(55L);
            return s;
        });
        service = new SuCoTaiXeServiceImpl(suCoRepo, chuyenRepo, taiKhoanRepo, taiXeRepo,
                mock(FileStorageService.class), notification);
    }

    private HttpStatus loi(Runnable r) {
        return assertThrows(AppException.class, r::run).getStatus();
    }

    @Test
    void thieuLoaiHoacMoTa_400() {
        assertEquals(HttpStatus.BAD_REQUEST, loi(() -> service.baoCaoSuCo("taixe01", 100L, null, "x", 2, null, null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, loi(() -> service.baoCaoSuCo("taixe01", 100L, "Khác", " ", 2, null, null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, loi(() -> service.baoCaoSuCo("taixe01", 100L, "Abc", "x", 2, null, null, null)));
    }

    @Test
    void khongCoChuyenDangThucHien_409() {
        when(chuyenRepo.timChuyenDangThucHien(10L)).thenReturn(List.of());
        assertEquals(HttpStatus.CONFLICT, loi(() -> service.baoCaoSuCo("taixe01", null, "Khác", "x", 2, null, null, null)));
    }

    @Test
    void chuyenDaHoanThanh_409() {
        chuyen.setTrangThai(5);
        assertEquals(HttpStatus.CONFLICT, loi(() -> service.baoCaoSuCo("taixe01", 100L, "Khác", "x", 2, null, null, null)));
    }

    @Test
    void suCoNghiemTrong_uuTienCao_vaBaoKhan() {
        SuCoResponse r = service.baoCaoSuCo("taixe01", 100L, "Tai nạn", "Va chạm nhẹ", 1, 21.0, 105.8, null);
        assertEquals(3, r.getMucDoUuTien());
        assertEquals("Mới tiếp nhận", r.getTenTrangThai());
        assertEquals(100L, r.getIdChuyen());
        assertNotNull(r.getViTri());
        verify(notification).notifyDispatchersOfIncident(55L, 100L, "Tai nạn", true);
    }

    @Test
    void khongTruyenIdChuyen_tuLayChuyenDangThucHien() {
        when(chuyenRepo.timChuyenDangThucHien(10L)).thenReturn(List.of(chuyen));
        SuCoResponse r = service.baoCaoSuCo("taixe01", null, "Tắc đường kéo dài", "Kẹt xe", null, null, null, null);
        assertEquals(100L, r.getIdChuyen());
        assertEquals(2, r.getMucDoUuTien());
    }
}
