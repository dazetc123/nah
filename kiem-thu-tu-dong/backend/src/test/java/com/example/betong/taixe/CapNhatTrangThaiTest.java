package com.example.betong.taixe;

import com.example.betong.dto.request.dieuphoi.GpsPingRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kiểm thử chức năng Cập nhật trạng thái (chuyến và xe) - phần backend. */
@DisplayName("Cập nhật trạng thái")
class CapNhatTrangThaiTest extends KiemThuTaiXeCoSo {

    private String toaDo(int trangThai, double viDo, double kinhDo) {
        return "{\"trangThai\": " + trangThai + ", \"viDo\": " + viDo + ", \"kinhDo\": " + kinhDo + "}";
    }

    @Test
    @DisplayName("CNTT-01 Cập nhật hành trình khi chưa nhận chuyến: trả 409")
    void cntt01_capNhatKhiChuaNhan() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyen(taoDonHangDaPhanBo(), tx);

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", toaDo(2, CT_VI_DO, CT_KINH_DO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Bạn cần nhận chuyến trước khi cập nhật hành trình"));
    }

    @Test
    @DisplayName("CNTT-02 Xác nhận đã đến khi cách công trình dưới 200 m: chuyến sang Đã đến, khách hàng được báo")
    void cntt02_daDenTrongBanKinh() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", toaDo(2, 21.0046, 105.7984))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(2))
                .andExpect(jsonPath("$.thoiGianDen").isNotEmpty());

        assertThat(trangThaiChuyen(idChuyen)).isEqualTo(2);
        assertThat(soThongBaoChoTaiKhoan(idTaiKhoanKhachCuaChuyen(idChuyen), "XE_DA_DEN")).isEqualTo(1);
    }

    @Test
    @DisplayName("CNTT-03 Xác nhận đã đến khi còn cách công trình 3,6 km: trả 400, trạng thái không đổi")
    void cntt03_daDenNgoaiBanKinh() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", toaDo(2, TRAM_VI_DO, TRAM_KINH_DO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bạn chưa tới gần công trình (còn cách khoảng 3619 m, cần trong 200 m)"));

        assertThat(trangThaiChuyen(idChuyen)).isEqualTo(3);
    }

    @Test
    @DisplayName("CNTT-05 Hoàn thành giao hàng: chuyến sang 8, đơn hàng sang Hoàn thành, xe vẫn bận")
    void cntt05_hoanThanhGiaoHang() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", toaDo(2, CT_VI_DO, CT_KINH_DO)).andExpect(status().isOk());

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", "{\"trangThai\": 8}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(8))
                .andExpect(jsonPath("$.thongBao").value("Đã hoàn thành giao hàng. Hãy bấm 'Đã về tới trạm' khi xe về tới trạm trộn"));

        assertThat(trangThaiDon(idDonCuaChuyen(idChuyen))).isEqualTo(4); // Đơn: Hoàn thành
        assertThat(trangThaiXe(tx.idXe())).isEqualTo(2);                  // Xe: còn đang giao (chưa về trạm)
        assertThat(soThongBaoChoVaiTro("Nhân viên điều phối", "HOAN_THANH_GIAO", idChuyen)).isEqualTo(1);
        assertThat(soThongBaoChoTaiKhoan(idTaiKhoanKhachCuaChuyen(idChuyen), "HOAN_THANH")).isEqualTo(1);
    }

    @Test
    @DisplayName("CNTT-06 Xác nhận đã về tới trạm: chuyến Hoàn thành, xe Sẵn sàng, vị trí xe đặt về trạm trộn")
    void cntt06_daVeToiTram() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", toaDo(2, CT_VI_DO, CT_KINH_DO)).andExpect(status().isOk());
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", "{\"trangThai\": 8}").andExpect(status().isOk());

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", "{\"trangThai\": 4}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(4))
                .andExpect(jsonPath("$.thongBao").value("Xe đã về tới trạm trộn và cập nhật địa chỉ trạm thành công"));

        assertThat(trangThaiXe(tx.idXe())).isEqualTo(1); // Xe: Sẵn sàng hoạt động
        GpsPingRequest viTri = (GpsPingRequest) redis.duLieu.get("xe:location:" + tx.idXe());
        assertThat(viTri.getViDo()).isEqualTo(TRAM_VI_DO);
        assertThat(viTri.getKinhDo()).isEqualTo(TRAM_KINH_DO);
        assertThat(soThongBaoChoVaiTro("Nhân viên điều phối", "HOAN_THANH", idChuyen)).isEqualTo(1);
    }

    @Test
    @DisplayName("CNTT-07 Báo cáo sự cố chuyến: chuyến Sự cố/Hủy, xe bảo trì, đơn quay về Đã phân bổ trạm, điều phối và quản lý được báo")
    void cntt07_baoCaoSuCo() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/bao-cao-su-co",
                "{\"lyDo\": \"Nổ lốp / chết máy xe\", \"viDo\": 21.02, \"kinhDo\": 105.79}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(7))
                .andExpect(jsonPath("$.tenTrangThai").value("Sự cố / Hủy"));

        assertThat(trangThaiXe(tx.idXe())).isEqualTo(0);                 // Xe: Bảo trì / Sự cố
        assertThat(trangThaiDon(idDonCuaChuyen(idChuyen))).isEqualTo(6); // Đơn: Đã phân bổ trạm (tạo chuyến thay thế)
        assertThat(db.queryForObject("SELECT muc_do_uu_tien FROM su_co WHERE id_chuyen = ?", Integer.class, idChuyen))
                .isEqualTo(3);                                            // Sự cố mức ưu tiên cao
        assertThat(soThongBaoChoVaiTro("Nhân viên điều phối", "SU_CO_HUY", idChuyen)).isEqualTo(1);
        assertThat(soThongBaoChoVaiTro("Quản lý", "SU_CO_HUY", idChuyen)).isEqualTo(1);
    }

    @Test
    @DisplayName("CNTT-09 Báo sự cố cho chuyến đã hoàn thành: trả 409")
    void cntt09_suCoChuyenDaHoanThanh() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", toaDo(2, CT_VI_DO, CT_KINH_DO)).andExpect(status().isOk());
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", "{\"trangThai\": 8}").andExpect(status().isOk());
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", "{\"trangThai\": 4}").andExpect(status().isOk());

        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/bao-cao-su-co", "{\"lyDo\": \"Thử báo sau khi xong\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Chuyến hàng đã hoàn thành, không thể báo cáo sự cố để hủy"));
    }

    @Test
    @DisplayName("CNTT-10 Chuyển xe sang bảo trì khi xe rảnh: thành công, xe không còn trong danh sách xe rảnh")
    void cntt10_xeSangBaoTri() throws Exception {
        TaiXeTest tx = taoTaiXe();

        appPut(tx, "/api/tai-xe/xe/trang-thai", "{\"trangThai\": 0}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value(0))
                .andExpect(jsonPath("$.tenTrangThai").value("Đang bảo trì"))
                .andExpect(jsonPath("$.thongBao").value("Đã cập nhật trạng thái xe"));

        assertThat(trangThaiXe(tx.idXe())).isEqualTo(0);
    }

    @Test
    @DisplayName("CNTT-11 Chuyển xe sang bảo trì khi đang có chuyến: trả 409, trạng thái xe không đổi")
    void cntt11_baoTriKhiDangCoChuyen() throws Exception {
        TaiXeTest tx = taoTaiXe();
        taoChuyenDaNhan(tx);

        appPut(tx, "/api/tai-xe/xe/trang-thai", "{\"trangThai\": 0}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Xe đang thực hiện chuyến, hãy hoàn thành chuyến trước khi chuyển sang bảo trì"));

        assertThat(trangThaiXe(tx.idXe())).isEqualTo(2);
    }

    @Test
    @DisplayName("CNTT-12 Xe sẵn sàng hoạt động trở lại sau sự cố: xe sang Sẵn sàng")
    void cntt12_xeSanSangTroLai() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/bao-cao-su-co", "{\"lyDo\": \"Hỏng xe\"}").andExpect(status().isOk());
        assertThat(trangThaiXe(tx.idXe())).isEqualTo(0);

        appPut(tx, "/api/tai-xe/xe/trang-thai", "{\"trangThai\": 1}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenTrangThai").value("Sẵn sàng hoạt động"));

        assertThat(trangThaiXe(tx.idXe())).isEqualTo(1);
        // Điều phối giao được chuyến mới cho xe này
        taoChuyen(taoDonHangDaPhanBo(), tx);
    }

    @Test
    @DisplayName("CNTT-13 Tài xế chưa được phân công xe: trả 404")
    void cntt13_chuaPhanCongXe() throws Exception {
        TaiXeTest tx = taoTaiXe(true, false);

        appGet(tx, "/api/tai-xe/xe")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bạn chưa được phân công xe nào"));
    }

    @Test
    @DisplayName("CNTT-14 Báo hỏng xe kèm ảnh: lưu báo cáo, xe chuyển sang bảo trì")
    void cntt14_baoHongXe() throws Exception {
        TaiXeTest tx = taoTaiXe();
        MockMultipartFile anh = new MockMultipartFile("anh", "xe-hong.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'});

        mvc.perform(multipart("/api/dieu-phoi/xe/bao-cao").file(anh)
                        .param("idXe", String.valueOf(tx.idXe()))
                        .param("trangThaiXe", "0")
                        .param("diaChiHu", "Khu vực gần tọa độ 21.02, 105.79")
                        .param("soDienThoaiTaiXe", "0900000001")
                        .param("nguyenNhan", "Hỏng hóc")
                        .param("noiDung", "Xe rò rỉ dầu")
                        .header("ClientKey", KEY_APP).header("Authorization", "Bearer " + tx.token()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.anhMinhChung").isNotEmpty())
                .andExpect(jsonPath("$.nguyenNhan").value("Hỏng hóc"));

        assertThat(trangThaiXe(tx.idXe())).isEqualTo(0);
    }
}
