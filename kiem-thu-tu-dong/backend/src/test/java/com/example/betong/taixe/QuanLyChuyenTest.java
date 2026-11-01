package com.example.betong.taixe;

import com.example.betong.entity.DonHang;
import com.example.betong.service.dieuphoi.ChuyenHetHanScheduler;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kiểm thử chức năng Quản lý chuyến (phần backend). */
@DisplayName("Quản lý chuyến")
class QuanLyChuyenTest extends KiemThuTaiXeCoSo {

    @Autowired ChuyenHetHanScheduler boQuetHetHan;

    @Test
    @DisplayName("QLC-01 Có đề nghị chuyến mới: hiện trong danh sách chờ xác nhận, hạn trả lời 5 phút")
    void qlc01_deNghiChuyenMoi() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyen(taoDonHangDaPhanBo(), tx);

        JsonNode ds = docJson(appGet(tx, "/api/tai-xe/chuyen/cua-toi?nhom=cho-xac-nhan").andExpect(status().isOk()));

        assertThat(ds).hasSize(1);
        assertThat(ds.get(0).get("idChuyen").asLong()).isEqualTo(idChuyen);
        assertThat(ds.get(0).get("tenTrangThai").asText()).isEqualTo("Chờ xác nhận");
        LocalDateTime phanCong = LocalDateTime.parse(ds.get(0).get("thoiGianPhanCong").asText());
        LocalDateTime han = LocalDateTime.parse(ds.get(0).get("hanXacNhan").asText());
        assertThat(han).isEqualTo(phanCong.plusMinutes(5));
    }

    @Test
    @DisplayName("QLC-02 Nhận chuyến: chuyến và đơn sang Đang giao, xe sang Đang giao hàng, điều phối được báo")
    void qlc02_nhanChuyen() throws Exception {
        TaiXeTest tx = taoTaiXe();
        DonHang don = taoDonHangDaPhanBo();
        Long idChuyen = taoChuyen(don, tx);

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/phan-hoi", "{\"chapNhan\": true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(3))
                .andExpect(jsonPath("$.thongBao").value("Bạn đã nhận chuyến. Bắt đầu lộ trình từ trạm trộn tới công trình!"));

        assertThat(trangThaiChuyen(idChuyen)).isEqualTo(3);  // Đang giao
        assertThat(trangThaiDon(don.getIdDH())).isEqualTo(3); // Đơn: Đang giao
        assertThat(trangThaiXe(tx.idXe())).isEqualTo(2);      // Xe: Đang giao hàng
        assertThat(soThongBaoChoVaiTro("Nhân viên điều phối", "TAI_XE_NHAN", idChuyen)).isEqualTo(1);
        assertThat(soThongBaoChoTaiKhoan(idTaiKhoanKhachCuaChuyen(idChuyen), "DON_DANG_GIAO")).isEqualTo(1);
    }

    @Test
    @DisplayName("QLC-03 Nhận chuyến khi chưa có GPLX: bị từ chối 403, chuyến vẫn chờ xác nhận")
    void qlc03_nhanChuyenThieuGPLX() throws Exception {
        TaiXeTest tx = taoTaiXe(false, true);
        Long idChuyen = taoChuyen(taoDonHangDaPhanBo(), tx);

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/phan-hoi", "{\"chapNhan\": true}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Tài xế chưa cập nhật Giấy phép lái xe (GPLX)")));

        assertThat(trangThaiChuyen(idChuyen)).isEqualTo(5);
    }

    @Test
    @DisplayName("QLC-04 Từ chối chuyến có lý do: chuyến sang Đã từ chối, lưu lý do, điều phối được báo")
    void qlc04_tuChoiChuyen() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyen(taoDonHangDaPhanBo(), tx);

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/phan-hoi",
                "{\"chapNhan\": false, \"lyDo\": \"Tôi đang bận, không thể nhận\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(6))
                .andExpect(jsonPath("$.lyDoTuChoi").value("Tôi đang bận, không thể nhận"))
                .andExpect(jsonPath("$.thongBao").value("Bạn đã từ chối chuyến"));

        assertThat(soThongBaoChoVaiTro("Nhân viên điều phối", "TAI_XE_TU_CHOI", idChuyen)).isEqualTo(1);
    }

    @Test
    @DisplayName("QLC-06 Hết hạn xác nhận: quá 5 phút chuyến tự sang Đã từ chối")
    void qlc06_hetHanXacNhan() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyen(taoDonHangDaPhanBo(), tx);
        // Giả lập chuyến được phân công từ 6 phút trước rồi chạy bộ quét hết hạn
        db.update("UPDATE chuyen SET thoi_gian_phan_cong = ? WHERE id_chuyen = ?",
                LocalDateTime.now().minusMinutes(6), idChuyen);

        boQuetHetHan.quetQuaHan();

        assertThat(trangThaiChuyen(idChuyen)).isEqualTo(6);
        assertThat(db.queryForObject("SELECT ly_do_tu_choi FROM chuyen WHERE id_chuyen = ?", String.class, idChuyen))
                .isEqualTo("Hết thời gian chờ tài xế xác nhận");
        assertThat(soThongBaoChoVaiTro("Nhân viên điều phối", "HET_HAN", idChuyen)).isEqualTo(1);
        assertThat(soThongBaoChoTaiKhoan(tx.idTK(), "HET_HAN")).isEqualTo(1);
    }

    @Test
    @DisplayName("QLC-07 Nhận chuyến đã hết hạn: trả 409")
    void qlc07_nhanChuyenDaHetHan() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyen(taoDonHangDaPhanBo(), tx);
        db.update("UPDATE chuyen SET thoi_gian_phan_cong = ? WHERE id_chuyen = ?",
                LocalDateTime.now().minusMinutes(6), idChuyen);
        boQuetHetHan.quetQuaHan();

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/phan-hoi", "{\"chapNhan\": true}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Chuyến này không còn ở trạng thái chờ xác nhận (có thể đã hết hạn)"));
    }

    @Test
    @DisplayName("QLC-08 Nhận chuyến được giao cho tài xế khác: trả 403")
    void qlc08_nhanChuyenCuaNguoiKhac() throws Exception {
        TaiXeTest chuChuyen = taoTaiXe();
        TaiXeTest nguoiKhac = taoTaiXe();
        Long idChuyen = taoChuyen(taoDonHangDaPhanBo(), chuChuyen);

        appPut(nguoiKhac, "/api/tai-xe/chuyen/" + idChuyen + "/phan-hoi", "{\"chapNhan\": true}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Bạn không được phân công chuyến này"));

        assertThat(trangThaiChuyen(idChuyen)).isEqualTo(5);
    }

    @Test
    @DisplayName("QLC-09 Xem chuyến đang thực hiện: đủ thông tin công trình, trạm trộn, khách hàng, xe")
    void qlc09_xemChuyenDangThucHien() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);

        appGet(tx, "/api/tai-xe/chuyen/cua-toi?nhom=hien-tai")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idChuyen").value(idChuyen))
                .andExpect(jsonPath("$[0].tenTrangThai").value("Đang giao"))
                .andExpect(jsonPath("$[0].khoiLuong").value(8.0))
                .andExpect(jsonPath("$[0].tenCongTrinh").value("Công trình test"))
                .andExpect(jsonPath("$[0].viDoCongTrinh").value(CT_VI_DO))
                .andExpect(jsonPath("$[0].viDoTram").value(TRAM_VI_DO))
                .andExpect(jsonPath("$[0].tenKhachHang").isNotEmpty())
                .andExpect(jsonPath("$[0].sdtKhachHang").isNotEmpty())
                .andExpect(jsonPath("$[0].bienSo").isNotEmpty());
    }

    @Test
    @DisplayName("QLC-10 Không có chuyến: danh sách chuyến hiện tại rỗng")
    void qlc10_khongCoChuyen() throws Exception {
        TaiXeTest tx = taoTaiXe();

        appGet(tx, "/api/tai-xe/chuyen/cua-toi?nhom=hien-tai")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("QLC-11 Xem lịch sử chuyến: có chuyến Hoàn thành và Sự cố/Hủy, mới nhất ở trên")
    void qlc11_xemLichSu() throws Exception {
        TaiXeTest tx = taoTaiXe();
        // Chuyến 1: giao xong và về trạm (Hoàn thành)
        Long chuyen1 = taoChuyenDaNhan(tx);
        appPut(tx, "/api/tai-xe/chuyen/" + chuyen1 + "/trang-thai",
                "{\"trangThai\": 2, \"viDo\": " + CT_VI_DO + ", \"kinhDo\": " + CT_KINH_DO + "}").andExpect(status().isOk());
        appPut(tx, "/api/tai-xe/chuyen/" + chuyen1 + "/trang-thai", "{\"trangThai\": 8}").andExpect(status().isOk());
        appPut(tx, "/api/tai-xe/chuyen/" + chuyen1 + "/trang-thai", "{\"trangThai\": 4}").andExpect(status().isOk());
        // Chuyến 2: báo sự cố (Sự cố / Hủy)
        Long chuyen2 = taoChuyenDaNhan(tx);
        appPut(tx, "/api/tai-xe/chuyen/" + chuyen2 + "/bao-cao-su-co", "{\"lyDo\": \"Nổ lốp\"}").andExpect(status().isOk());

        appGet(tx, "/api/tai-xe/chuyen/cua-toi?nhom=lich-su")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idChuyen").value(chuyen2))
                .andExpect(jsonPath("$[0].tenTrangThai").value("Sự cố / Hủy"))
                .andExpect(jsonPath("$[1].idChuyen").value(chuyen1))
                .andExpect(jsonPath("$[1].tenTrangThai").value("Hoàn thành"));
    }
}
