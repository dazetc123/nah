package com.example.betong.taixe;

import com.example.betong.dto.request.dieuphoi.GpsPingRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kiểm thử chức năng Gửi vị trí GPS (phần backend: POST /api/tai-xe/gps/ping). */
@DisplayName("Gửi vị trí GPS")
class GuiViTriGpsTest extends KiemThuTaiXeCoSo {

    private String ping(TaiXeTest tx, Long idChuyen, double viDo, double kinhDo) {
        return """
                {"idXe": %d, "idChuyen": %d, "viDo": %s, "kinhDo": %s, "tocDo": 30}
                """.formatted(tx.idXe(), idChuyen, viDo, kinhDo);
    }

    @Test
    @DisplayName("GPS-04 Gửi vị trí khi đang giao: vị trí mới nhất vào Redis, lịch sử vào bảng vi_tri_gps")
    void gps04_guiViTriKhiDangGiao() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);
        int truoc = soViTriGps(idChuyen);

        appPost(tx, "/api/tai-xe/gps/ping", ping(tx, idChuyen, 21.0200, 105.7950)).andExpect(status().isOk());

        GpsPingRequest viTri = (GpsPingRequest) redis.duLieu.get("xe:location:" + tx.idXe());
        assertThat(viTri.getViDo()).isEqualTo(21.0200);
        assertThat(viTri.getKinhDo()).isEqualTo(105.7950);
        assertThat(soViTriGps(idChuyen)).isEqualTo(truoc + 1);
    }

    @Test
    @DisplayName("GPS-05 Gửi liên tục: Redis luôn giữ vị trí mới nhất, CSDL chỉ ghi tối đa 1 điểm / 3 giây")
    void gps05_gioiHanGhiLichSu() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);
        int truoc = soViTriGps(idChuyen);

        appPost(tx, "/api/tai-xe/gps/ping", ping(tx, idChuyen, 21.0200, 105.7950)).andExpect(status().isOk());
        appPost(tx, "/api/tai-xe/gps/ping", ping(tx, idChuyen, 21.0100, 105.7970)).andExpect(status().isOk());

        GpsPingRequest viTri = (GpsPingRequest) redis.duLieu.get("xe:location:" + tx.idXe());
        assertThat(viTri.getViDo()).isEqualTo(21.0100);           // Redis: điểm thứ 2
        assertThat(soViTriGps(idChuyen)).isEqualTo(truoc + 1);   // CSDL: chỉ 1 điểm (2 lần gửi cách nhau < 3 giây)
    }

    @Test
    @DisplayName("GPS-09 Chuyến đã kết thúc: vị trí gửi lên bị bỏ qua, xe vẫn ở trạm trộn")
    void gps09_boQuaViTriSauKhiKetThuc() throws Exception {
        TaiXeTest tx = taoTaiXe();
        Long idChuyen = taoChuyenDaNhan(tx);
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai",
                "{\"trangThai\": 2, \"viDo\": " + CT_VI_DO + ", \"kinhDo\": " + CT_KINH_DO + "}").andExpect(status().isOk());
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", "{\"trangThai\": 8}").andExpect(status().isOk());
        appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", "{\"trangThai\": 4}").andExpect(status().isOk());
        int truoc = soViTriGps(idChuyen);

        appPost(tx, "/api/tai-xe/gps/ping", ping(tx, idChuyen, 21.0500, 105.8500)).andExpect(status().isOk());

        GpsPingRequest viTri = (GpsPingRequest) redis.duLieu.get("xe:location:" + tx.idXe());
        assertThat(viTri.getViDo()).isEqualTo(TRAM_VI_DO);   // vẫn là tọa độ trạm trộn
        assertThat(soViTriGps(idChuyen)).isEqualTo(truoc);   // không ghi thêm điểm
    }
}
